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

Etapas 1, 2 e 3 implementadas: apresentação, catálogo offline com seis livros e
tela de detalhes com autor, clima e sinopse. O botão “Ver detalhes” abre o livro
selecionado; “Voltar ao catálogo” ou o voltar do Android retorna à lista.

As sinopses são resumos escritos para o projeto. Os climas são uma curadoria,
não uma classificação indicativa. Não há textos completos dos livros.
Os livros e favoritos ficam no banco SQLite local `lumibooks.db`.
O catálogo inicial alimenta o banco apenas na criação. As telas consultam os
dados armazenados, e os favoritos permanecem após fechar o aplicativo.
As operações rodam fora da interface, em uma fila única.

Código organizado em modelo (`model/Book`), catálogo (`data/BookCatalog`)
e telas (`MainActivity` e `BookDetailsActivity`). `data/BancoLivros` cuida
do SQLite. Novas variáveis e IDs de tela usam português; nomes exigidos pelo
Android, como `onCreate`, seguem a API. As classes do catálogo anterior foram mantidas.

## Executar

Abra a pasta no Android Studio, sincronize o Gradle e execute o módulo app
em um emulador ou celular com Android 7.0 (API 24) ou superior.
Mantenha as versões de SDK e JDK indicadas pela configuração Gradle do projeto.

## Conferir a tela inicial

- Verifique o nome LumiBooks e os três climas de leitura.
- Role a tela em dispositivos menores.
- Confira fontes ampliadas e o tema escuro do sistema.
- Abra cada um dos seis livros e confira título, autor, clima e sinopse.
- Volte à lista pelo botão da tela e pelo voltar do Android.
- Gire o dispositivo na tela de detalhes e confira se o livro permanece correto.

## Acompanhamento da avaliação

| Critério | Situação após a etapa 3 |
| --- | --- |
| Funcionamento geral | Catálogo e navegação implementados; validação no dispositivo pendente |
| View customizada | Prevista na etapa 4; os layouts atuais não contam como esse requisito |
| Banco de dados | Implementado com SQLite: gravação e recuperação de livros e favoritos |
| Geolocalização | Prevista na etapa 5 |
| Sensores | Leitura real de luminosidade prevista na etapa 4 |
| Organização e documentação | Código separado e README atualizado a cada etapa |
| Apresentação | Preparação na etapa 6; todos os integrantes devem conhecer a implementação e estar presentes |
| Compilação automática e APK | GitHub Actions previsto na etapa 6 |

Testes locais do catálogo verificam identificadores únicos, busca de livros,
dados preenchidos e presença dos três climas. Execute
`gradlew.bat :app:testDebugUnitTest :app:assembleDebug :app:lintDebug` no Windows.

## Conferir os favoritos

1. Abra um livro e toque em “Adicionar aos favoritos”.
2. Volte ao catálogo e marque “Somente favoritos”.
3. Feche e reabra o aplicativo; filtre novamente e confira o livro salvo.
4. Abra o favorito, remova e volte: a lista filtrada deve atualizar.
5. Remova todos para conferir a mensagem de lista vazia.
6. Gire o aparelho na lista filtrada e nos detalhes para conferir o estado.

O teste `BancoLivrosTest` usa um banco separado e verifica carga inicial,
persistência ao reabrir, remoção e ausência de duplicação. Precisa de emulador
ou celular: `gradlew.bat :app:connectedDebugAndroidTest`.
Não foi executado nesta etapa por ausência de dispositivo conectado.
Limpar os dados ou desinstalar o aplicativo pode apagar os dados locais.

## Próximos requisitos

- View customizada: medidor gráfico de luminosidade.
- Banco de dados: livros, favoritos e histórico local.
- Geolocalização: local de uma recomendação, com permissão do usuário.
- Sensor: luminosidade real, com modo manual de demonstração como alternativa.
- Entrega: README e APK gerado por GitHub Actions.
