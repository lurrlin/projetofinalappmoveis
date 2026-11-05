# LumiBooks

Projeto desenvolvido para a Unidade Curricular de Aplicações Móveis.

## Funcionalidades

- **Descobrir:** recomenda livros de acordo com a luminosidade do ambiente.
- **Biblioteca:** catálogo inicial com 70 livros de temas variados e busca por título, autor ou gênero.
- **Open Library:** consulta e importação de novos livros, com capas e informações sobre as obras.
- **Favoritos:** seleção dos livros que você quer guardar.
- **Histórico:** registro das leituras com data e localização opcional, exibida como bairro, cidade e estado.

Os livros adicionados, os favoritos e o histórico ficam salvos no aparelho. A busca de novos livros, o carregamento de capas e a primeira consulta do endereço podem precisar de internet.

## Recursos do projeto

| Requisito | Implementação |
| --- | --- |
| View customizada | `CapaLivroView`: desenho da capa alternativa, lombada e marcador de favorito com Canvas e Paint. |
| Banco de dados | SQLite para armazenar livros, favoritos e histórico. |
| Geolocalização | Localização opcional da leitura, com consulta do endereço aproximado pelo serviço do Android. |
| Sensor | Sensor de luminosidade para ajustar as recomendações e as cores da interface. |
| Compilação automática | Workflow do GitHub Actions configurado para executar testes, analisar o código e gerar o APK. |

## Tecnologias

Java, XML, Android SDK, SQLite e [Open Library](https://openlibrary.org).
