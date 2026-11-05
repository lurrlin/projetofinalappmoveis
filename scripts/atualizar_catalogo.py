"""Atualização manual do catálogo educacional; consultas pequenas, cache e 1 req/s.
Execute da raiz: python scripts/atualizar_catalogo.py
Documentação: https://openlibrary.org/dev/docs/api/search
"""
import json
import time
import urllib.parse
import urllib.request
from datetime import datetime, timezone
from pathlib import Path

RAIZ = Path(__file__).resolve().parents[1]
TEMAS = [
    ("fantasy", "Fantasia", "LIGHT"),
    ("science_fiction", "Ficção científica", "MEDIUM"),
    ("romance", "Romance", "LIGHT"),
    ("mystery", "Mistério", "MEDIUM"),
    ("horror", "Terror", "DARK"),
    ("history", "História", "MEDIUM"),
    ("philosophy", "Filosofia", "MEDIUM"),
    ("brazilian_literature", "Literatura brasileira", "MEDIUM"),
]
ORIGINAIS = {"the hobbit", "harry potter and the philosopher's stone",
             "the adventures of sherlock holmes", "adventures of sherlock holmes", "dom casmurro", "the shining", "dracula"}

# Curadoria editorial: corrige títulos de edições antigas e temas excessivamente amplos da API.
AJUSTES = {
    "OL257943W": {"clima": "DARK"},
    "OL19351054W": {"titulo": "A Guerra da Papoula", "clima": "DARK"},
    "OL450063W": {"genero": "Terror", "clima": "DARK"},
    "OL64365W": {"titulo": "Admirável Mundo Novo", "clima": "DARK"},
    "OL52267W": {"titulo": "A Máquina do Tempo"},
    "OL20068530W": {"genero": "Suspense", "clima": "DARK"},
    "OL471565W": {"titulo": "E Não Sobrou Nenhum"},
    "OL244537W": {"genero": "Estratégia", "autor": "Sun Tzu"},
    "OL1968368W": {"genero": "Desenvolvimento pessoal"},
    "OL17732W": {"titulo": "Bíblia", "genero": "Religião", "autor": "Diversos autores"},
    "OL8193465W": {"genero": "Romance histórico"},
    "OL8193416W": {"genero": "Ficção gótica", "clima": "DARK"},
    "OL629052W": {"genero": "Política", "autor": "Karl Marx e Friedrich Engels"},
    "OL138052W": {"titulo": "Alice no País das Maravilhas", "genero": "Infantil", "clima": "LIGHT"},
    "OL45499W": {"autor": "Lao-Tsé"},
    "OL17837119W": {"genero": "Desenvolvimento pessoal"},
    "OL1003017W": {"titulo": "Memórias Póstumas de Brás Cubas"},
    "OL1002138W": {"titulo": "A Paixão Segundo G.H."},
    "OL1248157W": {"titulo": "Capitães da Areia", "autor": "Jorge Amado"},
    "OL27963469W": {"genero": "Terror", "clima": "DARK", "autor": "Luisa Geisler, Marcelo Ferroni, Natalia Borges Polesso e Samir Machado de Machado"},
}


def atualizar():
    cache = RAIZ / "build" / "openlibrary"
    cache.mkdir(parents=True, exist_ok=True)
    livros, chaves, consultas = [], set(), []
    for assunto, genero, clima in TEMAS:
        parametros = {"q": f"subject:{assunto} language:por", "limit": 24, "lang": "pt",
                      "fields": "key,title,author_name,cover_i,first_publish_year,editions,editions.key,editions.title"}
        url = "https://openlibrary.org/search.json?" + urllib.parse.urlencode(parametros)
        consultas.append(url)
        arquivo = cache / f"{assunto}.json"
        if not arquivo.exists():
            requisicao = urllib.request.Request(url, headers={"User-Agent": "LumiBooks/1.0 (educational project)"})
            with urllib.request.urlopen(requisicao, timeout=45) as resposta:
                arquivo.write_bytes(resposta.read())
            time.sleep(1.1)
        dados = json.loads(arquivo.read_text(encoding="utf-8"))
        quantidade = 0
        for item in dados["docs"]:
            chave = item["key"]
            if chave in chaves or item["title"].casefold() in ORIGINAIS:
                continue
            if not item.get("cover_i") or not item.get("author_name"):
                continue
            edicoes = item.get("editions", {}).get("docs", [])
            titulo = edicoes[0].get("title", item["title"]) if edicoes else item["title"]
            livros.append({"id": chave.rsplit("/", 1)[-1], "titulo": titulo,
                           "autor": item["author_name"][0], "genero": genero,
                           "clima": clima, "chave_open_library": chave,
                           "url_capa": f'https://covers.openlibrary.org/b/id/{item["cover_i"]}-M.jpg?default=false',
                           "sinopse": f'{genero} • Primeira publicação: {item.get("first_publish_year", "não informada")}. '
                                      'Sinopse não disponível. Saiba mais na ficha do livro.'})
            livros[-1].update(AJUSTES.get(livros[-1]["id"], {}))
            livros[-1]["sinopse"] = (f'{livros[-1]["genero"]} • Primeira publicação: {item.get("first_publish_year", "não informada")}. '
                                     'Sinopse não disponível. Saiba mais na ficha do livro.')
            chaves.add(chave)
            quantidade += 1
            if quantidade == 8:
                break
        print(f"{genero}: {quantidade} livros", flush=True)
    if len(livros) < 50:
        raise RuntimeError(f"Catálogo insuficiente: {len(livros)}. Arquivo anterior preservado.")
    destino = RAIZ / "app/src/main/assets/catalogo_open_library.json"
    destino.parent.mkdir(parents=True, exist_ok=True)
    destino.write_text(json.dumps({"fonte": "Open Library Search API", "consultado_em": datetime.now(timezone.utc).isoformat(),
                                  "consultas": consultas, "livros": livros}, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"Catálogo salvo: {len(livros)} livros reais.")


if __name__ == "__main__":
    atualizar()
