# Links

Postman: https://www.postman.com/amandaba-group/workspace/cp6-java/collection/58012781-e41b859b-efcd-49de-b596-23a3f49e31ec?action=share&source=copy-link&creator=58012781

Vídeo no Youtube: 
# Assistente de Literatura Clássica com IA Local

Aplicação web desenvolvida em Java e Spring Boot para oferecer um
assistente especializado em literatura clássica. O sistema recebe
perguntas em português, utiliza um modelo de inteligência artificial
executado localmente pelo Ollama para gerar respostas e mantém o
histórico das interações em banco de dados.

Este projeto foi desenvolvido como parte do **Checkpoint 6 (CP6) da FIAP
-- Java Advanced / Spring**, cujo objetivo é construir um assistente de
IA integrado a um provedor de modelos, disponibilizar uma API REST,
oferecer uma interface web e persistir o histórico de consultas.

## Objetivos

-   Disponibilizar um assistente virtual com foco em literatura
    clássica.
-   Receber perguntas dos usuários e gerar respostas em português
    brasileiro.
-   Executar o modelo de IA localmente por meio do Ollama, sem depender
    de chave de API ou créditos de um provedor externo.
-   Validar as mensagens recebidas pela API.
-   Registrar perguntas, respostas e datas das interações.
-   Consultar o histórico de interações em ordem cronológica
    decrescente.
-   Oferecer uma interface web simples para conversar com o assistente e
    visualizar respostas e registros anteriores.

## Tecnologias


| Tecnologia | Utilização |
| :--- | :--- |
| Java | Linguagem de programação |
| Spring Boot | Estrutura principal da aplicação |
| Spring Web | Exposição dos endpoints REST e comunicação HTTP |
| Spring Data JPA | Persistência e consulta dos registros |
| Hibernate | Implementação ORM utilizada pelo JPA |
| Maven | Gerenciamento de dependências e build |
| Ollama | Execução local do modelo de IA |
| Llama 3.2 | Modelo de linguagem configurado como padrão |
| HTML, CSS e JavaScript | Interface web |
| Thymeleaf | Renderização da página web pelo Spring Boot |
| JUnit 5 e Mockito | Testes automatizados |


## Arquitetura

O projeto segue uma organização em camadas, separando a exposição dos
endpoints, as regras de negócio e o acesso aos dados.


Pacote-base:

``` text
br.com.fiap.tdspo2.cp6_java
```


Principais componentes:


| Componente | Responsabilidade |
| :--- | :--- |
| `ChatController` | Expõe os endpoints REST relacionados ao chat. |
| `ChatPageController` | Disponibiliza a página web do assistente. |
| `ChatService` | Valida a mensagem recebida e coordena o processamento da interação. |
| `OllamaService` | Envia a pergunta ao Ollama local e obtém o conteúdo da resposta. |
| `HistoricoService` | Salva as interações e disponibiliza a consulta do histórico. |
| `HistoricoRepository` | Realiza a persistência e consulta dos registros de histórico. |
| `ChatMensagemRepository` | Realiza a persistência das mensagens de chat. |
| `Historico` | Entidade que representa um registro do histórico. |
| `ChatMensagem` | Entidade utilizada para representar uma mensagem do chat. |


### Fluxo de uma interação

1.  O usuário envia uma pergunta pela interface web ou diretamente pela
    API.
2.  O `ChatController` recebe a requisição e encaminha os dados ao
    `ChatService`.
3.  O `ChatService` valida a mensagem, rejeitando conteúdo nulo, vazio,
    composto apenas por espaços ou com mais de 280 caracteres.
4.  A mensagem validada é encaminhada ao `OllamaService`.
5.  O `OllamaService` envia a solicitação ao serviço local do Ollama e
    extrai o conteúdo retornado pelo modelo.
6.  A interação bem-sucedida é registrada no banco de dados por meio dos
    serviços e repositórios responsáveis.
7.  A aplicação retorna a resposta ao usuário.

## Integração com o Ollama

A aplicação utiliza uma instância local do Ollama para gerar as
respostas. A comunicação é realizada pelo endpoint nativo de chat:

``` text
http://localhost:11434/api/chat
```

A configuração é mantida em `application.properties`, permitindo alterar
o endereço do serviço e o modelo sem modificar o código:

``` properties
ollama.base-url=http://localhost:11434
ollama.model=llama3.2
```

O modelo configurado pode ser substituído por outro modelo instalado
localmente, desde que o nome corresponda ao apresentado pelo comando
`ollama list`.

### Parâmetros de geração


A requisição utiliza os seguintes parâmetros:


| Parâmetro | Valor | Finalidade |
| :--- | :--- | :--- |
| `stream` | `false` | Aguarda a resposta completa em vez de receber tokens progressivamente. |
| `num_predict` | `300` | Limita a quantidade de tokens gerados na resposta. |
| `temperature` | `0.7` | Controla a aleatoriedade da geração. |
| `top_p` | `0.9` | Controla a seleção de tokens durante a geração. |
| `num_ctx` | `4096` | Define o tamanho da janela de contexto utilizada pelo modelo. |


### Prompt de sistema

O prompt de sistema define o papel do modelo como um assistente
prestativo especializado em literatura clássica. Também orienta o modelo
a responder em português brasileiro e a respeitar os limites definidos
para a geração das respostas.

Como a inferência é realizada localmente, não é necessária uma chave de
API do provedor nem a contratação de créditos para cada requisição. O
download inicial do modelo exige conexão com a internet.

## Validação e tratamento de erros

O endpoint de chat recebe uma mensagem em JSON. O serviço valida o
conteúdo antes de consultar o modelo.

São consideradas inválidas as mensagens que:

-   Sejam nulas.
-   Estejam vazias.
-   Contenham somente espaços em branco.
-   Ultrapassem 280 caracteres.

As mensagens são normalizadas com a remoção de espaços excedentes nas
extremidades antes do processamento. Entradas inválidas devem resultar
em HTTP `400 Bad Request`.

A integração também trata situações em que não é possível estabelecer
comunicação com o Ollama, quando o serviço está indisponível ou quando o
modelo não retorna conteúdo utilizável. Nesses casos, a aplicação
retorna um status HTTP de erro e uma mensagem compreensível, sem expor
informações internas desnecessárias.

## API REST

### Enviar uma pergunta

**Endpoint:** `POST /api/chat`

**Content-Type:** `application/json`

Corpo da requisição:

``` json
{
  "message": "Explique a importância de Dom Casmurro."
}
```

A aplicação valida a mensagem, consulta o modelo configurado e retorna
os dados da interação conforme o contrato implementado pelo
`ChatController`.

### Consultar o histórico

**Endpoint:** `GET /api/historico`

Retorna os registros de histórico em ordem decrescente de data e hora,
apresentando primeiro as interações mais recentes.

A ordenação é realizada pelo método do repositório:

``` java
findAllByOrderByDataHoraDesc()
```

Exemplo ilustrativo de resposta:

``` json
[
  {
    "id": 2,
    "pergunta": "Explique a importância de Dom Casmurro.",
    "resposta": "Dom Casmurro é um dos romances mais conhecidos de Machado de Assis...",
    "dataHora": "2026-10-29T20:15:30"
  },
  {
    "id": 1,
    "pergunta": "O que caracteriza a literatura clássica?",
    "resposta": "A literatura clássica reúne obras que atravessam gerações...",
    "dataHora": "2026-10-29T20:10:15"
  }
]
```

Os valores acima são apenas exemplos. A estrutura exata da resposta
depende dos campos e serialização das entidades e DTOs utilizados no
projeto.

## Interface web

A aplicação disponibiliza uma interface web integrada ao Spring Boot,
utilizando Thymeleaf, HTML, CSS e JavaScript.

A interface permite:

-   Digitar e enviar perguntas ao assistente.
-   Visualizar as perguntas e respostas na área de conversa.
-   Acompanhar um indicador enquanto o modelo processa a solicitação.
-   Iniciar uma nova conversa na interface.
-   Consultar os registros do histórico e selecionar uma interação para
    visualizar sua pergunta e resposta.

A comunicação entre a interface e o backend é realizada por requisições
HTTP aos endpoints da própria aplicação.

Para acessar a interface, inicie o projeto e abra:

``` text
http://localhost:8080/
```

## Banco de dados

A aplicação utiliza Spring Data JPA para persistir e consultar os
registros. A entidade `Historico` representa as interações registradas,
incluindo pergunta, resposta e data/hora.

A consulta do histórico é ordenada pela data/hora em ordem decrescente.

Configure a conexão com o banco utilizado pelo projeto em
`application.properties` ou por variáveis de ambiente. Não versione
senhas ou outras credenciais reais.

Exemplo de propriedades --- ajuste o driver, URL, usuário e senha
conforme o banco efetivamente utilizado:

``` properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=update
```

Inclua no repositório o script SQL correspondente ao banco escolhido,
caso a criação das tabelas seja feita manualmente.

## Pré-requisitos

Antes de executar a aplicação, verifique se o ambiente possui:

-   JDK compatível com a versão configurada no projeto.
-   Maven ou Maven Wrapper incluído no repositório.
-   Ollama instalado e em execução.
-   O modelo configurado disponível localmente.
-   Uma instância do banco de dados configurada e acessível.

## Configuração e execução

### 1. Instalar o Ollama

Baixe e instale o Ollama:

https://ollama.com/download

No Windows, abra o PowerShell e confira a instalação:

``` powershell
ollama --version
```

### 2. Baixar o modelo

Baixe o modelo definido na configuração da aplicação:

``` powershell
ollama pull llama3.2
```

Confira os modelos instalados:

``` powershell
ollama list
```

Teste o modelo:

``` powershell
ollama run llama3.2
```

### 3. Iniciar o Ollama

Normalmente, o aplicativo do Ollama mantém o serviço em execução em
segundo plano. Se necessário, inicie o serviço:

``` powershell
ollama serve
```

Verifique a disponibilidade da API:

``` text
http://localhost:11434/api/tags
```

### 4. Configurar o banco de dados

Crie ou inicialize o banco utilizado pelo projeto e configure as
propriedades de conexão. Consulte os scripts SQL e as configurações de
exemplo disponibilizados no repositório.

### 5. Executar a aplicação

Na raiz do projeto, execute pelo Maven Wrapper:

Windows:

``` powershell
.\mvnw.cmd spring-boot:run
```

Linux ou macOS:

``` bash
./mvnw spring-boot:run
```

Depois, acesse a interface em `http://localhost:8080/`.

## Testes automatizados

Os testes automatizados utilizam JUnit 5 e Mockito para verificar o
comportamento dos serviços sem depender de uma instância real do Ollama
ou do banco de dados.

Entre os cenários contemplados ou previstos estão:

-   Validação de mensagens nulas, vazias e compostas somente por
    espaços.
-   Rejeição de mensagens que ultrapassem o limite de 280 caracteres.
-   Processamento de uma mensagem válida.
-   Persistência da mensagem de chat.
-   Tratamento de falhas na comunicação com o provedor.
-   Salvamento de registros de histórico.
-   Retorno da lista de histórico fornecida pelo repositório.
-   Comportamento quando não existem registros no histórico.

Os testes unitários do serviço de histórico verificam o encaminhamento
da consulta ordenada ao repositório. A garantia da ordenação efetiva dos
registros deve ser validada também com um teste de integração com o
banco de dados.

Para executar os testes:

``` powershell
.\mvnw.cmd test
```

## Exemplos de testes manuais

As requisições podem ser executadas pelo Postman.

/api/chat 200
<img width="1920" height="1080" alt="image" src="https://github.com/user-attachments/assets/9381a1b7-54c7-4d5f-a69f-4cafa861dbda" />

/api/chat 400 (Mensagem sem conteúdo)
<img width="1920" height="1080" alt="image" src="https://github.com/user-attachments/assets/3c23bb6c-708c-497e-b646-76df5ce05702" />

/api/chat 400 (Mensagem longa demais)
<img width="1920" height="1080" alt="image" src="https://github.com/user-attachments/assets/014c076e-7034-42a1-b036-1dc407718565" />

/api/historico 200
<img width="1920" height="1080" alt="image" src="https://github.com/user-attachments/assets/26c69e95-cddf-49f3-aa83-6da83d051552" />



| Cenário | Método | Endpoint | Resultado esperado |
| :--- | :--- | :--- | :--- |
| Enviar pergunta válida | POST | `/api/chat` | Resposta gerada pelo modelo. |
| Enviar mensagem vazia | POST | `/api/chat` | HTTP 400. |
| Enviar mensagem acima do limite | POST | `/api/chat` | HTTP 400. |
| Consultar histórico | GET | `/api/historico` | Registros ordenados por data decrescente. |
| Consultar com Ollama indisponível | POST | `/api/chat` | Erro tratado pela aplicação. |

## Estrutura de diretórios

A organização geral do projeto é:

``` text
cp6_java/
├── src/
│   ├── main/
│   │   ├── java/br/com/fiap/tdspo2/cp6_java/
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   ├── model/
│   │   │   ├── repository/
│   │   │   └── service/
│   │   └── resources/
│   │       ├── static/
│   │       │   ├── css/
│   │       │   └── js/
│   │       ├── templates/
│   │       └── application.properties
│   └── test/
│   │   └── java/br/com/fiap/tdspo2/cp6_java/service/
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.md
```

------------------------------------------------------------------------

**FIAP -- Checkpoint 6 \| Java Advanced / Spring**\
**Domínio do assistente:** Literatura clássica\
**Provedor de IA:** Ollama local
