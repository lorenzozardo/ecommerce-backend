# E-commerce — Backend (API REST)

API REST de um e-commerce com cadastro de categorias e produtos, tratamento de erros padronizado, busca paginada
com filtros, relatório de produtos por categoria e atualização parcial de estoque.

A interface web que consome esta API está no repositório
[ecommerce-frontend](https://github.com/lorenzozardo/ecommerce-frontend).

## Tecnologias

- Java 17 e Spring Boot 4.0.7 (Maven)
- Spring Web MVC, Spring Data JPA e Bean Validation
- Banco H2 em memória (o driver do PostgreSQL já está incluído no projeto)
- SpringDoc OpenAPI (Swagger UI), Lombok e DevTools

## Como executar

Pré-requisito: **JDK 17 ou superior**. Não é preciso instalar o Maven, pois o projeto usa o Maven Wrapper (`mvnw`).

```bash
# Windows
mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

A API sobe em **http://localhost:8080**.

| Recurso | Endereço |
|---|---|
| API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html (a raiz `/` também redireciona para ele) |
| OpenAPI (JSON) | http://localhost:8080/v3/api-docs |
| Console do H2 | http://localhost:8080/h2-console (JDBC URL `jdbc:h2:mem:ecommerce`, usuário `sa`, senha em branco) |

Ao iniciar, o Hibernate cria as tabelas e executa o `src/main/resources/import.sql`, que cadastra
**3 categorias e 13 produtos** de exemplo. Como o banco é em memória, os dados voltam ao estado inicial a cada reinício.

### Porta 8080 ocupada?

A porta pode ser trocada pela variável de ambiente `PORT` (lembre de ajustar o `VITE_API_URL` do frontend):

```bash
# Windows (PowerShell)
$env:PORT=8081; .\mvnw.cmd spring-boot:run

# Linux / macOS
PORT=8081 ./mvnw spring-boot:run
```

## Endpoints

### Categorias (`/categorias`)

| Método | URL | Descrição | Sucesso |
|---|---|---|---|
| `POST` | `/categorias` | Cria uma categoria (`{"nome": "..."}`) | `202` |
| `GET` | `/categorias` | Lista todas as categorias (com seus produtos) | `200` |
| `GET` | `/categorias/{id}` | Busca por id | `200` |
| `PUT` | `/categorias/{id}` | Atualiza o nome | `200` |
| `DELETE` | `/categorias/{id}` | Remove (somente se não tiver produtos) | `204` |

### Produtos (`/produtos`)

| Método | URL | Descrição | Sucesso |
|---|---|---|---|
| `POST` | `/produtos` | Cria um produto (`ProdutoRequestDTO`) | `202` |
| `GET` | `/produtos?nome=&categoriaId=&page=0&size=10&ordenarPor=nome` | Busca paginada com filtros opcionais | `200` |
| `GET` | `/produtos/todos` | Lista todos (sem paginação) | `200` |
| `GET` | `/produtos/{id}` | Busca por id | `200` |
| `PUT` | `/produtos/{id}` | Atualiza todos os dados | `200` |
| `PATCH` | `/produtos/{id}/estoque` | Atualiza só o estoque (`{"estoque": 50}`) | `200` |
| `DELETE` | `/produtos/{id}` | Remove | `204` |
| `GET` | `/produtos/relatorio/por-categoria` | Quantidade de produtos por categoria | `200` |

Parâmetros da busca paginada:

| Parâmetro | Padrão | Descrição |
|---|---|---|
| `nome` | — | Trecho do nome, sem diferenciar maiúsculas/minúsculas |
| `categoriaId` | — | Id da categoria |
| `page` | `0` | Página (começa em 0) |
| `size` | `10` | Itens por página (1 a 100) |
| `ordenarPor` | `nome` | `id`, `nome`, `preco` ou `estoque` |

A resposta segue o formato do `Page` do Spring:

```json
{
  "content": [ { "id": 9, "nome": "Headset HyperX", "descricao": "Surround 7.1 USB", "preco": 499.99,
                 "estoque": 12, "categoriaId": 2, "nomeCategoria": "Periféricos" } ],
  "totalElements": 13, "totalPages": 2, "number": 0, "size": 10, "first": true, "last": false
}
```

### Exemplos (curl)

```bash
curl http://localhost:8080/categorias
curl "http://localhost:8080/produtos?nome=notebook&categoriaId=1&page=0&size=5&ordenarPor=preco"
curl http://localhost:8080/produtos/relatorio/por-categoria
curl -X POST http://localhost:8080/produtos -H "Content-Type: application/json" -d "{\"nome\": \"Notebook Dell\", \"descricao\": \"Notebook i7 16GB RAM\", \"preco\": 4999.99, \"estoque\": 10, \"categoriaId\": 1}"
curl -X PATCH http://localhost:8080/produtos/1/estoque -H "Content-Type: application/json" -d "{\"estoque\": 99}"
```

## Respostas de erro

| Situação | Status | Corpo |
|---|---|---|
| Recurso não encontrado (`RecursoNaoEncontradoException`) | `404` | texto, ex.: `Categoria não encontrada` |
| Regra de negócio (`RegraNegocioException`) | `400` | texto, ex.: `Categoria não encontrada` ao criar produto com `categoriaId` inválido |
| Campos inválidos (`MethodArgumentNotValidException`) | `400` | JSON `{ "campo": "mensagem" }` |
| JSON mal formatado ou parâmetro com tipo errado | `400` | texto explicando o problema |
| Violação de integridade no banco | `409` | texto |

As mensagens em texto são enviadas como `text/plain; charset=UTF-8`.

## Testes

```bash
mvnw.cmd test      # Windows
./mvnw test        # Linux / macOS
```

São 38 testes de integração (MockMvc) cobrindo o CRUD, as validações, as respostas de erro, a paginação, os filtros,
a ordenação, o relatório, o PATCH de estoque e o CORS.

## Estrutura

```
src/main/java/br/unisinos/ecommerce
├── EcommerceApplication.java
├── config/       CorsConfig
├── controller/   CategoriaController, ProdutoController
├── dto/          ProdutoRequestDTO, ProdutoResponseDTO, AtualizarEstoqueDTO
├── entity/       Categoria, Produto
├── exception/    RecursoNaoEncontradoException, RegraNegocioException, GlobalExceptionHandler
├── repository/   CategoriaRepository, ProdutoRepository, ProdutoPorCategoriaProjection
└── service/      CategoriaService, ProdutoService
src/main/resources
├── application.properties
└── import.sql
```

## Decisões de implementação

- Entidades JPA separadas dos dados de entrada e saída da API (`ProdutoRequestDTO` e `ProdutoResponseDTO`, como
  records): o cliente envia só o `categoriaId` e recebe o nome da categoria junto do produto.
- Exceções de domínio tratadas em um único `@RestControllerAdvice`, com status e mensagens padronizados.
- Uma categoria com produtos vinculados não pode ser excluída (`400` com mensagem, em vez de erro do banco).
- Validações com mensagens em português: estoque não negativo e preço positivo com no máximo 2 casas decimais.
- A coluna `descricao` aceita 500 caracteres, o mesmo limite da validação.
- `ordenarPor`, `page` e `size` são validados; a ordenação por nome ignora maiúsculas/minúsculas e usa o id como
  desempate, para nenhum item se repetir entre páginas.
- Relatório por categoria feito com uma projeção JPQL (`ProdutoPorCategoriaProjection`).
- O `import.sql` é lido em UTF-8, evitando acentos quebrados no Windows.
- Origens do CORS e porta configuráveis (`app.cors.allowed-origins` / `APP_CORS_ALLOWED_ORIGINS` e `PORT`).

## Observações

- O aviso `Serializing PageImpl instances as-is is not supported...` no log é esperado: a busca paginada devolve o
  `Page<ProdutoResponseDTO>` diretamente, mantendo o formato JSON usado pelo frontend.
