# LumiBooks

Biblioteca interativa que recomendará livros conforme a luminosidade do ambiente.
Desenvolvida em Java com layouts XML no Android Studio.

## Etapas

1. Base e tela inicial com a identidade LumiBooks.
2. Catálogo de livros e detalhes.
3. Banco de dados local e favoritos.
4. Sensor de luminosidade, recomendações e medidor customizado.
5. Geolocalização opcional e histórico de recomendações.
6. Testes finais, documentação e GitHub Actions para gerar o APK.

Cada etapa será entregue separadamente para commit manual.

## Estado atual

A primeira etapa apresenta os três climas de leitura. Ainda não há catálogo,
armazenamento, leitura de sensores ou localização.

## Executar

Abra a pasta no Android Studio, sincronize o Gradle e execute o módulo app
em um emulador ou celular com Android 7.0 (API 24) ou superior.
Mantenha as versões de SDK e JDK indicadas pela configuração Gradle do projeto.

## Conferir a tela inicial

- Verifique o nome LumiBooks e os três climas de leitura.
- Role a tela em dispositivos menores.
- Confira fontes ampliadas e o tema escuro do sistema.

## Requisitos planejados

- View customizada: medidor gráfico de luminosidade.
- Banco de dados: livros, favoritos e histórico local.
- Geolocalização: local de uma recomendação, com permissão do usuário.
- Sensor: luminosidade real, com modo manual de demonstração como alternativa.
- Entrega: README e APK gerado por GitHub Actions.
