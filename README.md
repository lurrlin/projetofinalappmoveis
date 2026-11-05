# LumiBooks

Uma biblioteca interativa que recomenda livros conforme a luz ao seu redor.
Em ambientes escuros, sugere terror e suspense; com luz intermediária, mistério
e drama; com bastante luz, aventura e fantasia.

Aplicativo Android desenvolvido em **Java e XML**, com catálogo offline de seis
livros, favoritos, medidor gráfico próprio e histórico com localização opcional.
As sinopses foram escritas para o projeto. O aplicativo não disponibiliza os
textos completos dos livros. Os climas são uma curadoria, não classificação indicativa.

## Requisitos do trabalho

| Critério | Pontos previstos na avaliação | Implementação |
| --- | ---: | --- |
| Funcionamento geral | 2,0 | Catálogo, detalhes, recomendações, favoritos e histórico |
| View customizada | 1,0 | `MedidorLuminosidade`, desenhado com Canvas e Paint |
| Banco de dados | 1,0 | SQLite: livros, favoritos e histórico; migração da versão 1 para 2 |
| Geolocalização | 1,0 | Localização opcional ao salvar um registro, com permissão |
| Sensores | 1,0 | Leitura de luminosidade por TYPE_LIGHT |
| Organização e documentação | 1,0 | Classes por responsabilidade, testes, README e roteiro |
| Apresentação | 1,0 | [Roteiro de apresentação](docs/APRESENTACAO.md); todos devem participar |
| Compilação automática e APK | 2,0 | [GitHub Actions](.github/workflows/android.yml) com testes locais, análise e APK |
| Total | 10,0 | A pontuação depende da avaliação e da demonstração |

As seis etapas de implementação estão preparadas. A primeira execução da
automação no GitHub e a demonstração dos sensores em aparelho físico ainda
precisam ser confirmadas pelo grupo.

## Como usar

### Nova interface — branch melhorias

A navegação inferior reúne Descobrir, Biblioteca, Favoritos e Histórico.
Biblioteca mostra todos os livros; Favoritos mostra somente os salvos, sem filtro
de luz. O Histórico usa cartões com capa, data e clima; as coordenadas aparecem
ao tocar em “Ver local deste registro”.

O aplicativo permanece em modo escuro. Os realces dos botões e da navegação mudam
com o sensor: vermelho para pouca luz, azul para luz intermediária e verde para
luz forte. Sem leitura disponível, o realce inicial é azul e o catálogo permanece acessível.

As capas vêm da [Open Library](https://openlibrary.org/developers/api), com IDs
conferidos pela busca pública. Algumas imagens são de edições em inglês; os títulos
e resumos do catálogo continuam em português. A primeira carga requer internet.
As imagens ficam em cache local; sem imagem ou conexão, aparece um símbolo de livro.
O banco está na versão 3 e a migração mantém favoritos e histórico.

Este primeiro commit visual ainda usa seis livros e as recomendações por luz.
Os próximos commits serão a importação do catálogo e as recomendações que priorizam
histórico e favoritos. Não foi identificada documentação pública oficial atual
para uma integração de catálogo do Skoob; por isso a alternativa adotada foi Open Library.

1. Abra o LumiBooks. O sensor começa a medir a luz na tela inicial.
2. Veja o clima no medidor e os livros recomendados.
3. No emulador, use **⋮ → Virtual sensors → Additional sensors → Light** para ajustar os lux.
4. Abra **Biblioteca** para explorar todos os livros, independentemente da luz.
5. Abra **Ver detalhes** para ler a sinopse e adicionar ou remover um favorito.
6. Use **Favoritos** na navegação inferior para acessar os livros salvos.
7. Nos detalhes, toque em **Registrar no histórico** e depois **Salvar registro**.
8. Marque a opção de localização apenas se quiser incluí-la.
9. Acesse **Ver histórico** no catálogo ou na tela de registro.

Não há slider ou modo manual dentro do app. Sem leitura, Descobrir apresenta todos
os livros e informa a ausência do sensor. O sensor virtual do emulador envia os mesmos
eventos TYPE_LIGHT usados em um aparelho físico.
Veja os [controles do emulador](https://developer.android.com/studio/run/emulator-extended-controls).
Alguns controles exigem abrir o emulador em janela separada.

## Abrir no Android Studio

- Android mínimo para executar: **7.0 / API 24**.
- SDK de compilação e destino: **37**, pacote `platforms;android-37.0`.
- Android Build Tools: **36.0.0**.
- JDK para executar o Gradle: **25**, conforme `gradle/gradle-daemon-jvm.properties`.
- Gradle Wrapper: **9.6.0**; Android Gradle Plugin: **9.4.1**.
- O código Java usa compatibilidade de linguagem Java 11.

Abra a pasta no Android Studio, instale o SDK indicado pelo SDK Manager,
sincronize o Gradle e execute o módulo `app`. A primeira sincronização precisa
de internet para baixar dependências. `local.properties` contém o caminho do
SDK da sua máquina e não deve ser enviado ao GitHub.

No Windows:

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:assembleDebugAndroidTest
```

No Linux/macOS:

```sh
bash ./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:assembleDebugAndroidTest
```

## APK e entrega

O APK gerado localmente fica em `app/build/outputs/apk/debug/app-debug.apk`.
Na preparação da entrega local, também foi disponibilizada uma cópia em
`build/entrega/LumiBooks.apk`. Arquivos de build são ignorados pelo Git.

Para baixar o APK gerado automaticamente:

1. Envie o projeto, incluindo `.github/workflows/android.yml` e o Gradle Wrapper,
   para o repositório do grupo no GitHub.
2. Abra **Actions → Compilar LumiBooks**.
3. Aguarde a execução ficar verde.
4. Em **Artifacts**, baixe **LumiBooks-APK** e extraia `LumiBooks.apk`.
5. Entregue esse APK junto com o link do repositório e seu README.

O workflow executa em pushes, pull requests e manualmente por **Run workflow**.
Ele executa testes locais e lint, compila o aplicativo e o APK de testes,
e publica o APK apenas quando essas verificações passam. Não executa um emulador
no GitHub. Os relatórios ficam no artefato **LumiBooks-relatorios**.

O APK fica disponível por 30 dias; os relatórios, por 14 dias. Guarde a versão
da entrega ou anexe-a manualmente a uma Release do repositório. É um APK **debug
assinado automaticamente**, adequado à demonstração acadêmica; não é uma versão
de publicação na Play Store. Builds de máquinas diferentes podem usar assinaturas
de debug diferentes e não permitir atualização por cima. Não desinstale sem
considerar que isso pode apagar os favoritos e o histórico locais.

O workflow foi preparado seguindo a documentação do
[setup-java](https://github.com/actions/setup-java/tree/v5),
[setup-android](https://github.com/android-actions/setup-android),
[setup-gradle](https://github.com/gradle/actions/tree/v5/setup-gradle) e
[upload-artifact](https://github.com/actions/upload-artifact/tree/v4).
Sua execução remota ainda não foi confirmada: a pasta não tinha remoto
configurado durante a preparação da entrega.

## Como funcionam as recomendações

| Primeira leitura do sensor | Clima |
| --- | --- |
| 0 até menos de 50 lux | Entre sombras |
| 50 até menos de 300 lux | Meia-luz |
| A partir de 300 lux | Luz do dia |

As faixas são decisões do projeto. No sensor real, pequenas margens evitam
trocar os livros repetidamente perto dos limites: sombras passa a meia-luz a
partir de 60 lux, e meia-luz volta a sombras abaixo de 40. Meia-luz passa a luz
do dia a partir de 330, e luz do dia volta abaixo de 270. Mudanças grandes podem
pular diretamente de uma ponta à outra. O sensor virtual usa as mesmas regras do sensor físico.

`SensorLuminosidade` registra TYPE_LIGHT apenas enquanto a tela inicial está
em primeiro plano. Ao retornar, aguarda uma nova leitura. Valores negativos,
infinitos ou inválidos são descartados. O catálogo é atualizado quando o clima
muda, sem consultar o banco a cada evento.

`MedidorLuminosidade` estende `View` e usa `onDraw`, `Canvas` e `Paint`
para desenhar três arcos e um ponteiro. O ponteiro indica uma categoria, não
uma escala numérica. O valor em lux aparece no texto e o medidor tem descrição
para leitor de tela.

## Banco, histórico e localização

O SQLite local `lumibooks.db` armazena os livros e favoritos. O catálogo inicial
é inserido apenas na criação do banco. A versão 2 acrescenta o histórico sem
apagar livros ou favoritos. As operações são executadas em uma fila fora da interface.

Cada registro contém livro, data/hora do salvamento, luminosidade e clima capturados
ao abrir os detalhes, e indicação de leitura real ou simulada. Sem leitura, o
histórico informa essa ausência. A tela exibe até 100 registros mais recentes;
os demais permanecem armazenados.

A localização começa desmarcada e é solicitada apenas ao salvar com essa opção.
O aplicativo aceita permissão aproximada ou precisa, consulta provedores disponíveis
e aguarda até 20 segundos. Aceita posições com até 30 segundos de idade e precisão
informada pelo Android. Mostra latitude, longitude e precisão estimada; não oferece
mapa ou endereço.

Se a permissão for negada, o serviço estiver desligado ou a posição não chegar,
o registro é salvo sem coordenadas, com uma mensagem explicativa. Sair da tela
durante a busca cancela a coleta; é necessário tocar em salvar novamente.
Não há localização em segundo plano ou envio de histórico e coordenadas a um servidor.
O download de capas faz requisições à Open Library com o identificador da capa,
sem enviar o histórico, os favoritos ou a localização. O banco
foi excluído do backup automático e da transferência entre dispositivos nas regras
do Android. Limpar os dados ou desinstalar o aplicativo pode apagá-lo.

## Organização do código

Tudo fica em `app/src/main/java/com/example/projetofinal`:

| Parte | Responsabilidade |
| --- | --- |
| `MainActivity` | Catálogo, filtros e integração com luminosidade |
| `BookDetailsActivity` | Sinopse e favoritos |
| `SalvarHistoricoActivity` | Formulário, permissão e registro |
| `HistoricoActivity` | Consulta do histórico |
| `data/BookCatalog` | Catálogo inicial |
| `data/BancoLivros` | SQLite e migração |
| `model/Book`, `model/RegistroHistorico` | Dados dos livros e registros |
| `luminosidade/` | Sensor e regras de recomendação |
| `localizacao/Localizador` | Busca pontual e cancelamento |
| `view/MedidorLuminosidade` | Desenho customizado |

Novas variáveis e IDs usam português. Os nomes exigidos pelo Android, como
`onCreate`, seguem a API. Classes do catálogo da segunda etapa conservaram seus nomes.

## Testes e revisão final

Na revisão de entrega, a compilação passou, os **8 testes locais** e os
**8 testes no emulador** passaram, e o lint terminou **sem erros nem avisos**.
As imagens das quatro telas foram conferidas. O medidor recebeu uma correção
para não cortar as bordas em telas de alta densidade; os três testes das telas
foram repetidos após esse ajuste.

- Testes locais: catálogo, busca por ID, faixas, estabilidade e seleção das recomendações.
- Testes Android: favoritos, histórico, migração sem perda de dados, validação e ordenação.
- Testes de telas: Biblioteca completa, detalhes, salvamento sem localização e preservação da aba ao recriar.
- Lint: revisão estática do código e dos recursos Android.

Com um emulador ou celular conectado:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest
```

Se o executor de testes Gradle não estiver disponível no cache offline, após compilar
os APKs é possível executá-los diretamente com as ferramentas do Android SDK:

```sh
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb shell am instrument -w com.example.projetofinal.test/androidx.test.runner.AndroidJUnitRunner
```

Os testes de banco usam arquivos separados, removidos ao final. O teste de tela que
salva um histórico remove somente o registro criado por ele.

Antes da apresentação, confira no aparelho do grupo:

- Sensor real com luz variando, sem depender apenas da simulação.
- Permissão precisa, aproximada e negada; localização desligada e sem sinal.
- Favoritos e histórico após fechar e reabrir.
- Rotação durante a leitura, após salvar e durante a busca de localização.
- Tela pequena, tema sempre escuro e fonte ampliada.
- Primeira execução verde do GitHub Actions e download do APK.

## Etapas dos commits

1. Base e tela inicial.
2. Catálogo e detalhes.
3. Banco e favoritos.
4. Luminosidade e view customizada.
5. Histórico e localização.
6. Revisão, documentação e geração automática do APK.

## Grupo e apresentação

Preencha os dados antes da entrega:

- Integrantes: **a preencher pelo grupo**.
- Turma e unidade curricular: **a preencher pelo grupo**.
- Repositório e versão entregue: **a preencher pelo grupo**.

Todos os integrantes devem estar presentes e saber explicar a implementação.
Consulte o [roteiro de apresentação](docs/APRESENTACAO.md).
