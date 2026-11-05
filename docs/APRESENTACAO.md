# Apresentação do LumiBooks

## Preparação

- Instale e abra o APK antes da apresentação.
- Use um celular com sensor de luminosidade e localização funcionando.
- Separe uma fonte de luz e teste como cobrir o sensor do aparelho.
- Prepare pelo menos um favorito e um registro com localização.
- Tenha o README, o código e a execução verde do GitHub Actions disponíveis.
- Preencha os nomes dos integrantes no README e combine quem demonstra cada parte.

## Demonstração sugerida — cerca de 8 minutos

1. **Ideia (1 minuto):** uma biblioteca que adapta recomendações à luz do ambiente.
   Explique os três climas e mostre o catálogo.
2. **Sensor e view customizada (2 minutos):** varie a luz no sensor real e mostre
   a frase, as cores e os livros mudando. Explique o desenho próprio das capas
   e mostre o marcador de favorito. Os realces usam vermelho, amarelo e verde. No
   emulador, use More → Virtual sensors → Additional sensors → Light.
3. **Banco e favoritos (1 minuto):** favorite um livro, abra a aba Favoritos, feche e
   reabra o app. Explique que os dados são recuperados do SQLite.
4. **Localização e histórico (2 minutos):** abra um livro, registre com localização
   e abra “Ver local deste registro” no histórico. Explique que a permissão pode ser negada
   e que o app continua funcionando sem coordenadas.
5. **Código e testes (1 minuto):** mostre as classes responsáveis por sensor,
   desenho, banco e localização. Apresente os resultados dos testes.
6. **Automação e entrega (1 minuto):** mostre uma execução verde do GitHub Actions,
   o download do APK e o README.

## O que todos precisam saber explicar

- O sensor entrega luminosidade em lux; ele não usa a câmera nem o brilho da tela.
- As faixas de recomendação são escolhas do grupo. Pequenas margens evitam trocas
  repetidas perto dos limites.
- A view customizada é CapaLivroView, com Canvas e Paint para capa alternativa,
  lombada, recorte e marcador de favorito. O medidor antigo foi removido.
- Os livros, favoritos e registros ficam no SQLite. As migrações acrescentam
  histórico e capas sem apagar os dados anteriores.
- As consultas ao banco não bloqueiam a interface.
- Cada tela interrompe sua leitura do sensor ao perder o primeiro plano.
- A localização é opcional, tem limite de espera e não é coletada em segundo plano.
- Novos registros guardam livro, data e localização opcional, sem lux. Campos
  antigos são preservados na migração, mas ficam fora da interface.
- Os 64 livros adicionais vieram da Open Library. A busca online permite escolher
  outros livros para importar; repetir a importação não duplica a obra.
- O GitHub Actions compila, roda testes locais e lint e gera um APK debug.
- A simulação e os testes em emulador não substituem a demonstração no celular.

## Limites assumidos

Catálogo inicial de 70 livros, ampliável pela Open Library, sem leitura integral,
login, mapas ou sincronização. Capas e edições podem ter outros idiomas.
Livros importados não têm sinopse fornecida pela Search API; há link para a ficha.
As recomendações continuam por luz; personalização pelo histórico está pendente.
O histórico mostra até 100 registros recentes. O APK é destinado à demonstração.
A avaliação depende do funcionamento demonstrado e do conhecimento de todo o grupo.
