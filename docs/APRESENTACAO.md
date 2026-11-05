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
   o valor em lux, o ponteiro e os livros mudando. Explique que os arcos são
   desenhados no código e os realces mudam entre vermelho, azul e verde. No
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
- A view customizada é a classe MedidorLuminosidade, com Canvas e Paint.
- Os livros, favoritos e registros ficam no SQLite. As migrações acrescentam
  histórico e capas sem apagar os dados anteriores.
- As consultas ao banco não bloqueiam a interface.
- Cada tela interrompe sua leitura do sensor ao perder o primeiro plano.
- A localização é opcional, tem limite de espera e não é coletada em segundo plano.
- A luminosidade salva é a de quando os detalhes foram abertos; a localização é
  obtida ao salvar. Registros antigos feitos no modo manual mantêm essa indicação.
- O GitHub Actions compila, roda testes locais e lint e gera um APK debug.
- A simulação e os testes em emulador não substituem a demonstração no celular.

## Limites assumidos

Catálogo local de seis livros, sem leitura integral, login, mapas ou sincronização.
O histórico mostra até 100 registros recentes. O APK é destinado à demonstração.
A avaliação depende do funcionamento demonstrado e do conhecimento de todo o grupo.
