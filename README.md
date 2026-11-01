# LumiBooks

Biblioteca interativa que recomenda livros conforme a luminosidade do ambiente.
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

Etapas 1 a 4 implementadas: apresentação, catálogo offline com seis livros e
tela de detalhes com autor, clima e sinopse. O botão “Ver detalhes” abre o livro
selecionado; “Voltar ao catálogo” ou o voltar do Android retorna à lista.

A tela inicial lê a luminosidade, mostra um medidor próprio e filtra livros pelo
clima do ambiente. É possível combinar recomendações e favoritos ou desmarcar
os dois filtros para acessar o catálogo inteiro.

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

`luminosidade/SensorLuminosidade` cuida do sensor;
`luminosidade/ClassificadorLuminosidade` define as faixas e as recomendações;
`view/MedidorLuminosidade` estende `View` e desenha os arcos e o ponteiro em
`onDraw`, usando `Canvas` e `Paint`. Não é uma imagem pronta nem um medidor
de biblioteca. O ponteiro indica uma das três categorias, não um valor numérico;
o valor em lux é mostrado no texto. O medidor tem descrição para leitor de tela.

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

| Critério | Situação após a etapa 4 |
| --- | --- |
| Funcionamento geral | Catálogo e navegação implementados; validação no dispositivo pendente |
| View customizada | Implementada: MedidorLuminosidade desenhado com Canvas e Paint |
| Banco de dados | Implementado com SQLite: gravação e recuperação de livros e favoritos |
| Geolocalização | Prevista na etapa 5 |
| Sensores | TYPE_LIGHT integrado; teste em aparelho físico pendente |
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

- Etapa 5: geolocalização e histórico local das recomendações.
- Etapa 6: validação final, apresentação, README final e APK gerado por GitHub Actions.

## Luminosidade e recomendações

As faixas são escolhas do projeto:

| Primeira leitura ou simulação | Clima |
| --- | --- |
| De 0 até menos de 50 lux | Entre sombras |
| De 50 até menos de 300 lux | Meia-luz |
| A partir de 300 lux | Luz do dia |

No sensor real, há uma margem para evitar mudanças repetidas: sombras só passa
para meia-luz a partir de 60 lux; meia-luz só volta a sombras abaixo de 40 lux.
Meia-luz passa a luz do dia a partir de 330 lux; luz do dia só volta abaixo de
270 lux. Mudanças grandes podem pular diretamente de sombras para luz do dia
ou vice-versa. O modo manual usa as faixas exatas da tabela.

O sensor só fica registrado enquanto a tela inicial está em primeiro plano.
Ao retornar, o aplicativo aguarda uma nova leitura. Leituras negativas, infinitas
ou inválidas são descartadas. A lista é atualizada quando o clima muda, sem
consultar o banco a cada evento do sensor.

Sem sensor, o aplicativo ativa a simulação automaticamente. Uma falha ao
registrar o sensor também ativa esse modo e mostra uma mensagem. Se o sensor
demorar a enviar a primeira leitura, uma mensagem explica como usar a simulação
ou consultar o catálogo inteiro. A simulação não substitui a demonstração do
sensor real exigida pelo trabalho.

## Conferir a etapa 4

1. Ative “Simular luminosidade” e mova a barra para 0, 200 e 1.000 lux.
2. Confira o ponteiro e os livros: terror, mistério/drama e aventura/fantasia.
3. Desmarque “Recomendar pela luminosidade” para ver todos os seis livros.
4. Combine o filtro de luminosidade com “Somente favoritos”; confira também
   a mensagem quando nenhum favorito combina com o clima.
5. Gire o aparelho: modo manual, valor simulado e filtros devem ser mantidos.
6. Em aparelho com sensor, desative a simulação e varie a luz que chega ao sensor.
   Confira que o valor é identificado como real e que os livros mudam.
7. Abra um livro e volte; no modo real, a leitura deve ser retomada.
8. Confira a tela em tema claro/escuro, com fonte ampliada e leitor de tela.

Na etapa 4, a compilação, os testes locais e o lint passaram (zero erros,
cinco avisos já existentes). Quatro testes novos cobrem faixas, estabilidade,
leituras inválidas e recomendações. A aparência e o sensor real ainda precisam
ser verificados em dispositivo; nenhum estava conectado durante esta etapa.
