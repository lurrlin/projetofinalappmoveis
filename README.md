# LumiBooks

Uma biblioteca Android em **Java e XML** que adapta sugestões à luz do ambiente.
Tema sempre escuro, estante de capas, busca, favoritos e histórico com localização opcional.

## Catálogo e Open Library

O aplicativo inclui **70 livros**: os seis originais e **64 obras consultadas na
Open Library**, disponíveis no SQLite desde a primeira abertura, mesmo offline.
Há fantasia, ficção científica, romance, mistério, terror, história, filosofia,
literatura brasileira e outros temas revisados pelo projeto.

Os dados importados estão em `app/src/main/assets/catalogo_open_library.json`.
O arquivo registra a origem, a data da consulta e as URLs utilizadas.
Os IDs das obras e das capas vêm da API real. Alguns títulos, autores e temas
receberam ajustes editoriais; os climas não são classificação indicativa.

Na aba **Biblioteca**:

1. Digite um título, autor ou tema para filtrar os livros locais.
2. Para encontrar outros livros, toque em **Buscar na Open Library**.
3. Selecione os resultados desejados e toque em **Adicionar selecionados**.
4. Os livros escolhidos ficam salvos no aparelho. Importar novamente a mesma
   obra não duplica registros nem substitui favoritos e histórico.

A busca online consulta até 12 obras por vez, com preferência por edições em
português. Títulos, idiomas, capas e qualidade dos metadados podem variar.
As respostas são reaproveitadas por cinco minutos, com no máximo uma consulta
por segundo. Erros de conexão não impedem usar a biblioteca local.

As capas carregam fora da interface e ficam em cache no aparelho. Sem imagem,
o componente próprio desenha uma capa alternativa. O app não baixa textos completos.
A Search API não retorna sinopses: os seis livros originais têm resumos próprios;
os importados mostram os dados bibliográficos disponíveis e um link para a ficha,
sem inventar resumos.

Fontes oficiais: [Search API](https://openlibrary.org/dev/docs/api/search),
[Covers API](https://openlibrary.org/dev/docs/api/covers) e
[regras de uso](https://openlibrary.org/developers/api).

Para atualizar manualmente o catálogo incluído:

```powershell
python scripts/atualizar_catalogo.py
```

O script faz oito consultas pequenas por tema, reutiliza respostas em
`build/openlibrary/` e valida a quantidade antes de substituir o arquivo.
Requer Python 3 e internet na primeira consulta. Alterações futuras no catálogo
incluído devem vir acompanhadas de uma nova migração para instalações existentes.

## Interface e luminosidade

- **Descobrir:** sugestões pelo clima de luz, com uma frase curta colorida.
- **Biblioteca:** todos os livros, busca por título/autor/tema e importação online.
- **Favoritos:** estante dos livros salvos, sem filtro por luminosidade.
- **Histórico:** capa, título, data e acesso opcional ao local registrado.

Os realces mudam entre **vermelho** (pouca luz), **amarelo** (meia-luz) e
**verde** (ambiente claro). Não há termômetro, slider ou valor de lux na interface.
Sem leitura disponível, os livros continuam acessíveis; o realce inicial é amarelo.

A estante usa RecyclerView para reaproveitar cartões e carregar as capas conforme
aparecem. A fonte é sans-serif, com contraste e tamanhos consistentes. Telas largas
usam três colunas; fontes ampliadas usam uma coluna.

| Primeira leitura | Clima |
| --- | --- |
| Menos de 50 lux | Entre sombras |
| 50 até menos de 300 lux | Meia-luz |
| A partir de 300 lux | Luz do dia |

As margens do sensor evitam trocas repetidas perto dos limites: 60/40 lux na
primeira transição e 330/270 na segunda. Valores inválidos são descartados.
Cada tela interrompe sua leitura ao sair do primeiro plano.

No emulador, ajuste a luz em **⋮ → Virtual sensors → Additional sensors → Light**.
Alguns controles exigem uma janela separada do emulador.
[Documentação Android](https://developer.android.com/studio/run/emulator-extended-controls).

As recomendações desta etapa continuam baseadas no clima. Personalização pelo
histórico e pelos favoritos ainda é uma etapa pendente.

## Requisitos do trabalho

| Critério | Pontos | Implementação |
| --- | ---: | --- |
| Funcionamento geral | 2,0 | Biblioteca, busca, importação, detalhes, favoritos e histórico |
| View customizada | 1,0 | `CapaLivroView`: Canvas, Paint, recorte, lombada, capa alternativa e marcador de favorito |
| Banco de dados | 1,0 | SQLite, catálogo de 70 livros e migrações que preservam os dados |
| Geolocalização | 1,0 | Localização opcional ao registrar um livro |
| Sensores | 1,0 | TYPE_LIGHT altera sugestões, frase e cores |
| Organização e documentação | 1,0 | Classes por responsabilidade, testes, README e roteiro |
| Apresentação | 1,0 | [Roteiro](docs/APRESENTACAO.md); todos devem participar |
| Compilação automática e APK | 2,0 | [GitHub Actions](.github/workflows/android.yml) |
| Total | 10,0 | Depende da avaliação e demonstração do grupo |

A view customizada passou do antigo medidor para as capas. `CapaLivroView`
estende AppCompatImageView e sobrescreve `onDraw` e `onMeasure`: desenha a
alternativa offline, a lombada e o marcador de favorito com Canvas e Paint,
recorta a imagem e mantém a proporção da capa. É usada na estante, nos detalhes
e no histórico.

## Banco, histórico e localização

O SQLite `lumibooks.db` está na **versão 4**. A migração acrescenta gênero,
referência da Open Library e os 64 novos livros, preservando IDs, favoritos e
registros anteriores. Consultas e gravações ficam fora da interface.
A importação usa transação e verifica IDs para evitar duplicação.

Novos registros guardam livro, data/hora e localização opcional. **Não gravam
lux nem clima.** Os campos antigos permanecem por compatibilidade, mas não
aparecem na interface. O histórico mostra os últimos 100 registros.

A localização começa desmarcada e só é solicitada ao salvar com essa opção.
Aceita permissão aproximada ou precisa, espera até 20 segundos e aceita posições
de até 30 segundos de idade. Sem permissão ou sinal, salva sem coordenadas.
Sair da tela cancela a busca. Latitude, longitude e precisão ficam disponíveis
em **Ver local deste registro**; não há coleta em segundo plano.

Histórico, favoritos e coordenadas não são enviados à Open Library. Apenas
a busca digitada e os IDs de capas são consultados online. O banco está excluído
do backup automático e da transferência entre aparelhos. Desinstalar ou limpar
os dados pode apagar a biblioteca pessoal.

## Abrir e compilar

- Android mínimo: **7.0 / API 24**.
- SDK de compilação/destino: **37**, pacote `platforms;android-37.0`.
- Build Tools: **36.0.0**.
- JDK: **25**, conforme `gradle/gradle-daemon-jvm.properties`.
- Gradle Wrapper: **9.6.0**; Android Gradle Plugin: **9.4.1**.
- Código Java compatível com Java 11.

Abra a pasta no Android Studio, instale o SDK e sincronize o Gradle.
A primeira sincronização precisa de internet. Não envie `local.properties`.

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:assembleDebugAndroidTest
```

No Linux/macOS, use `bash ./gradlew` com as mesmas tarefas.

## Testes

- 8 testes locais: catálogo original, IDs e regras da luminosidade.
- 13 testes Android: banco, migrações das versões 1 e 3, catálogo ampliado,
  parser da API, importação sem duplicatas, busca, rotação e navegação.
- 1 teste opcional com a API real: consulta, importação em banco isolado e seletor.
- Lint e revisão visual das telas.

Com emulador conectado:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest
```

Alternativa após compilar os APKs:

```sh
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb shell am instrument -w com.example.projetofinal.test/androidx.test.runner.AndroidJUnitRunner
```

O teste de rede fica desabilitado por padrão para não depender de serviços externos.
Para executá-lo explicitamente:

```sh
adb shell am instrument -w -e verificar_rede true -e class com.example.projetofinal.OpenLibraryRedeTest com.example.projetofinal.test/androidx.test.runner.AndroidJUnitRunner
```

Os testes de banco usam arquivos isolados e removem seus dados ao terminar.
O teste de histórico no app remove apenas o próprio registro.

## APK e automação

APK local: `app/build/outputs/apk/debug/app-debug.apk`.
Cópia de entrega: `build/entrega/LumiBooks.apk`. Arquivos de build são ignorados pelo Git.

O workflow executa em pushes, pull requests e manualmente, testa e compila o app,
e publica **LumiBooks-APK** e **LumiBooks-relatorios** nos artefatos do GitHub Actions.
A execução remota precisa ser conferida pelo grupo. O workflow compila os testes
Android, mas não executa emulador no GitHub.

O APK debug é adequado à demonstração acadêmica. Builds de máquinas diferentes
podem ter assinaturas diferentes; não desinstale sem considerar os dados locais.
Os artefatos duram 30 dias para APK e 14 dias para relatórios; guarde a entrega.

## Organização

Os arquivos Java ficam em `app/src/main/java/com/example/projetofinal`.

| Parte | Responsabilidade |
| --- | --- |
| `MainActivity` | Abas, busca, sensor e seleção da importação |
| `BookDetailsActivity` | Livro, favoritos e ficha de origem |
| `SalvarHistoricoActivity`, `HistoricoActivity` | Registro e histórico |
| `data/BancoLivros` | SQLite e migrações |
| `data/CatalogoOpenLibrary` | Catálogo incluído, Search API e parser |
| `data/BookCatalog` | Seis livros originais |
| `ui/EstanteAdapter`, `ui/CarregadorCapas` | Estante e cache de imagens |
| `ui/AtividadeTematica` | Cores conforme o sensor |
| `view/CapaLivroView` | Componente gráfico customizado |
| `luminosidade/`, `localizacao/` | Sensor, classificação e localização |

Novas variáveis e IDs usam português. Nomes exigidos pelo Android e classes
originais do catálogo conservam seus nomes.

## Grupo e apresentação

- Integrantes: **a preencher pelo grupo**.
- Turma e unidade curricular: **a preencher pelo grupo**.
- Repositório e versão entregue: **a preencher pelo grupo**.

Todos devem estar presentes e saber explicar o código. Antes da apresentação,
confira sensor físico, permissões de localização, funcionamento offline,
favoritos após reabrir, fonte ampliada e a execução verde do GitHub Actions.
