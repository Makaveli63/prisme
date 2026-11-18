// Downloads every feed listed in feeds.json into <out>/cache/ so the web app
// can read them from its own origin (no CORS proxy needed).
// Usage: node scripts/fetch-feeds.mjs [outDir]
import { readFile, writeFile, mkdir } from 'node:fs/promises';
import { createHash } from 'node:crypto';

const OUT = (process.argv[2] || '_site') + '/cache';
const UA = 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/18.0 Safari/605.1.15';
const norm = u => (u || '').trim().replace(/^http:\/\//i, 'https://').replace(/\/+$/, '').toLowerCase();
const isFeed = t => /<(rss|feed|rdf:RDF)[\s>]/i.test(t.slice(0, 4000));

async function get(url) {
  const r = await fetch(url, {
    redirect: 'follow', signal: AbortSignal.timeout(20000),
    headers: { 'user-agent': UA, accept: 'application/rss+xml, application/atom+xml, application/xml;q=0.9, text/xml;q=0.9, text/html;q=0.8, */*;q=0.5', 'accept-language': 'fr-FR,fr;q=0.9,en;q=0.8' },
  });
  if (!r.ok) throw new Error('HTTP ' + r.status);
  return { text: await r.text(), url: r.url };
}

function discover(html, base) {
  const out = [];
  for (const tag of html.match(/<link\b[^>]*>/gi) || []) {
    if (!/rel=["']?[^"'>]*alternate/i.test(tag)) continue;
    if (!/type=["']?application\/(rss|atom)\+xml/i.test(tag)) continue;
    const m = /href=["']([^"']+)["']/i.exec(tag);
    if (m) try { out.push(new URL(m[1].replace(/&amp;/g, '&'), base).href); } catch {}
  }
  return out;
}

async function resolve(feed) {
  const tried = new Set();
  const attempt = async u => {
    if (!u || tried.has(u)) return null;
    tried.add(u);
    try { const r = await get(u); return { ...r, feed: isFeed(r.text) }; } catch (e) { return { error: e.message }; }
  };
  let r = await attempt(feed.url);
  if (r?.feed) return r;
  const candidates = r?.text ? discover(r.text, r.url) : [];
  if (!candidates.length && feed.site) {
    const home = await attempt(feed.site);
    if (home?.feed) return home;
    if (home?.text) candidates.push(...discover(home.text, home.url));
  }
  const origin = new URL(feed.site || feed.url).origin;
  for (const p of ['/feed', '/rss', '/feed/', '/rss.xml', '/feed.xml', '/atom.xml']) candidates.push(origin + p);
  for (const c of candidates) { const x = await attempt(c); if (x?.feed) return x; }
  throw new Error(r?.error || 'no feed found');
}

const feeds = JSON.parse(await readFile('feeds.json', 'utf8'));
await mkdir(OUT, { recursive: true });
const index = {};
let ok = 0;
await Promise.all(feeds.map(async feed => {
  try {
    const r = await resolve(feed);
    const file = createHash('sha1').update(r.url).digest('hex').slice(0, 16) + '.xml';
    await writeFile(`${OUT}/${file}`, r.text);
    const entry = { file, url: r.url, t: Date.now() };
    for (const k of [feed.url, feed.site, r.url]) if (k) index[norm(k)] = entry;
    ok++;
    console.log('OK  ', feed.title, '->', r.url);
  } catch (e) {
    console.log('FAIL', feed.title, e.message);
  }
}));
await writeFile(`${OUT}/index.json`, JSON.stringify(index));
console.log(`${ok}/${feeds.length} feeds cached`);
