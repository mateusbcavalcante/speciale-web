# Speciale (SPDM) — Documentação do Sistema

Sistema de gestão da **Speciale Panificações** (“Pães Artesanais”). Nome interno do projeto: **SPDM**.

Este repositório (`speciale-web`) contém o **portal administrativo web** (WAR `spdm`). A solução completa é multi-módulo e depende dos demais projetos do monorepo.

---

## Índice

- [Visão geral](#visão-geral)
- [Arquitetura em camadas](#arquitetura-em-camadas)
- [Módulos do ecossistema](#módulos-do-ecossistema)
- [Fontes de dados](#fontes-de-dados)
- [Fluxos detalhados](#fluxos-detalhados)
- [Validações de pedido](#validações-de-pedido)
- [API REST (speciale-ws)](#api-rest-speciale-ws)
- [Ponto de atenção arquitetural](#ponto-de-atenção-arquitetural)
- [Stack tecnológica](#stack-tecnológica)

---

## Visão geral

O Speciale cobre o ciclo operacional da padaria:

- Pedidos de clientes B2B (web e app mobile)
- Cadastros (clientes, produtos, usuários, feriados)
- Produção e logística (relatórios internos)
- Integração com o ERP **Omie**
- Integração com **AtivMob** (sugestões de pedido)

Há **dois frontends** e **dois deployáveis** de backend, compartilhando a mesma lógica de negócio (`speciale-service`):

| Canal | Módulo | Tecnologia |
|-------|--------|------------|
| Portal administrativo | `speciale-web` (este repo) | JSF 2 + RichFaces + AdminLTE |
| App mobile (cliente) | `speciale-service` (Ionic/Angular) | Ionic 5 + Angular 8 |
| API REST | `speciale-ws` | Jersey (JAX-RS) |
| Negócio + domínio | `speciale-service` | Java 8 + Hibernate |
| Biblioteca compartilhada | `speciale-commum` | Java 8 (JAR `brcmn`) |

---

## Arquitetura em camadas

```
┌─────────────────────────────────────────────────────────────────┐
│                        CANAIS DE ACESSO                          │
├──────────────────────────────┬──────────────────────────────────┤
│  Portal Web (speciale-web)   │  App Mobile (go-bread-app)        │
│  JSF / RichFaces / AdminLTE  │  Ionic / Angular                  │
└──────────────┬───────────────┴──────────────────┬───────────────┘
               │                                   │
               ▼                                   ▼
┌──────────────────────────┐         ┌────────────────────────────┐
│  WAR spdm (direto)       │         │  WAR spdmws (Jersey REST)  │
│  Managed Beans → Service   │         │  PedidoWS, ClientesWS...   │
└──────────────┬───────────┘         └─────────────┬──────────────┘
               │                                   │
               └──────────────┬────────────────────┘
                              ▼
               ┌──────────────────────────────┐
               │  speciale-service (JAR)      │
               │  Entidades, Services, Omie   │
               └──────────────┬───────────────┘
                              │
               ┌──────────────┴───────────────┐
               ▼                              ▼
    ┌────────────────────┐        ┌────────────────────┐
    │  speciale-commum   │        │  PostgreSQL        │
    │  Usuários, permissões│        │  Cadastros, relatórios│
    └────────────────────┘        └────────────────────┘
                                              │
                              ┌───────────────┴───────────────┐
                              ▼                               ▼
                    ┌─────────────────┐           ┌─────────────────┐
                    │  Omie ERP       │           │  AtivMob        │
                    │  Pedidos, preços│           │  Sugestões      │
                    └─────────────────┘           └─────────────────┘
```

---

## Módulos do ecossistema

### `speciale-commum` (pasta: `speciale-commum`)

Biblioteca compartilhada JAR (`brcmn`, artefato Maven `br.com.a2dm:brcmn:1.2`). Base reutilizável da A2DM.

| Responsabilidade | Exemplos |
|------------------|----------|
| Infraestrutura | `HibernateUtil`, `A2DMHbNgc` |
| Segurança / acesso | `Usuario`, `Grupo`, `TelaAcao`, `AutorizacaoFilter` |
| Utilitários | `AbstractBean`, `JSFUtil`, e-mail, CEP, validações, JasperReports |
| DTOs compartilhados | `PedidoDTO`, `ProdutoDTO`, `ClienteIntegracaoDTO` |

Não roda sozinho: é dependência de `speciale-service` e chega ao `speciale-web` de forma transitiva.

---

### `speciale-service`

Camada de negócio (`spdm-service`, JAR) — coração do domínio Speciale.

| Área | Conteúdo |
|------|----------|
| Domínio | `Pedido`, `Cliente`, `Produto`, `Receita`, `Feriado`, `NaoConformidade`, etc. |
| Regras | `PedidoService`, `ClienteService`, `ProdutoService`, sugestões, avisos |
| Integração Omie | Pacote `omie.*` — pedidos, clientes, produtos, tabelas de preço |
| App mobile | Projeto **Ionic/Angular** (`go-bread-app`) no mesmo módulo |

O app mobile consome a API REST (`speciale-ws`), por exemplo: `https://specialepanificacoes.com/spdmws_v2`.

---

### `speciale-web` (este repositório)

**Portal administrativo web** (`spdm`, WAR).

| Item | Detalhe |
|------|---------|
| Empacotamento | WAR Maven (`artifactId`: `spdm`) |
| UI | JSF 2 + RichFaces + AdminLTE |
| Padrão | Managed Beans (`*Bean.java`) + páginas `.xhtml` em `spdm/src/main/webapp/pages/` |
| Autenticação | Sessão HTTP + `AutorizacaoFilter` (do `commum`) |
| Sistema | `numSistema=3` em `web.xml` |

**Perfis e menus principais:**

| Perfil | Funcionalidades |
|--------|-----------------|
| **Cliente** | Pedidos |
| **Gerente** | Gerador de pedido, manutenção (usuários, clientes, feriados, sugestões, avisos, não conformidades) |
| **Funcionário / Gerente** | Relatórios: produção do dia, produção por período, logística do dia, observações |

Beans principais: `PedidoBean`, `GeradorPedidoBean`, `ProducaoDiaBean`, `LogisticaDiaBean`, `ClienteBean`, `SugestaoPedidoBean`, `LoginBean`, etc.

---

### `speciale-ws`

**API REST** (`spdmws`, WAR) — JSON via **Jersey** para o app mobile e integrações externas.

| Endpoint | Função |
|----------|--------|
| `POST /seguranca/login` | Autenticação mobile |
| `POST /seguranca/recuperarSenha` | Recuperação de senha |
| `PUT /seguranca/alterarSenha` | Alteração de senha |
| `GET/POST/PUT /pedidos` | Consultar, criar, alterar, inativar pedidos (Omie) |
| `GET /produtos/clientes/{id}` | Catálogo de produtos por cliente (Omie) |
| `GET /produtos/estrutura/{id}` | Estrutura/BOM do produto |
| `GET /clientes/...` | Busca e webhook Omie |
| `GET /opcoesEntregas` | Opções de entrega e frete |
| `POST /ativmob/sugestoesPedido/{cnpj}` | Pull de sugestões AtivMob |
| `POST /ativmob/cadastrar-evento` | Webhook de eventos AtivMob |

Inclui filtro **CORS** para consumo pelo app mobile.

---

## Fontes de dados

O sistema opera com **duas fontes** que convivem no dia a dia:

| Dado | Fonte principal | Uso |
|------|-----------------|-----|
| Pedidos (criar, editar, consultar, cancelar) | **Omie ERP** | Web cliente, app mobile, gerador, sugestões |
| Cadastros auxiliares (cliente, produto, opção entrega, feriado, usuário) | **PostgreSQL** | Validações, permissões, vínculos |
| Relatórios de produção e logística | **PostgreSQL** (`ped.tb_pedido`, `ped.tb_pedido_produto`) | Painel interno |

Os fluxos atuais de **criação de pedido** gravam no Omie via `OmiePedidoService` → `OmiePedidoRepository` (`IncluirPedido`, `ListarPedidos`, etc.). O método `PedidoService.inserir()` que persiste no banco local existe, mas **não é chamado** pelos fluxos principais atuais (web/mobile/gerador).

---

## Fluxos detalhados

### 1. Autenticação — Portal web

```
Usuário → login.jsf (LoginBean)
       → MD5(senha maiúscula)
       → UsuarioService.get(login, senha, flgAtivo=S)
       → session.loginUsuario = usuario
       → redirect principal.jsf

Requisições *.jsf → AutorizacaoFilter
       → sem sessão? redirect login.jsf
       → com sessão? AbstractBean.validarAcesso() + ValidaPermissao (grupo × tela × ação)
```

- Recuperação de senha: `recuperarSenha.jsf` / `RecuperarSenhaBean`
- Timeout de sessão: 30 minutos (`web.xml`)

---

### 2. Autenticação — App mobile

```
App → POST /seguranca/login { login, senha }
    → SegurancaWS → MD5 → UsuarioService
    → idGrupo = 10 (cliente)? → salva usuário no localStorage → /app
    → idGrupo = 11 (admin)? → logout + "acesso não permitido"
```

- Apenas **clientes** (`idGrupo === 10`) usam o app
- Sem JWT: autenticação stateless no cliente (localStorage)
- API WS não aplica filtro de autenticação nas demais rotas

---

### 3. Pedido — App mobile (cliente B2B)

```
Login
  → Home / Produtos
  → GET /produtos/clientes/{idCliente}  → Omie (catálogo + preços)
  → Adicionar ao carrinho (CarrinhoStore — memória local)
  → Carrinho → revisar itens e observações
  → POST /pedidos
  → OmiePedidoService.cadastrarPedido → validações → Omie IncluirPedido
  → retorna número do pedido
```

| Ação | API |
|------|-----|
| Pesquisar | `GET /pedidos?idCliente&dataPedido&idPedido` |
| Alterar | `PUT /pedidos/{idPedido}` |
| Inativar | `PUT /pedidos/{id}/inativar` |

Componentes principais: `PedidosService`, `LojaService`, `CarrinhoStore`, `PedidoStore`.

---

### 4. Pedido — Portal web (perfil Cliente)

Fluxo via `PedidoBean` — mesma regra de negócio do mobile.

```
preparaInserir
  → valida cliente.idExternoOmie e idTabelaPrecoOmie (banco local)
  → OmieProdutoService.listarProdutosPorCliente
  → usuário monta itens na sessão

pesquisar
  → OmiePedidoService.pesquisarPedido (Omie ListarPedidos)

inserir / alterar
  → PedidoService.inserirPedido / alterarPedido
  → buildPedido → PedidoDTO
  → OmiePedidoService.cadastrarPedido / alterarPedido

inativar
  → OmiePedidoService.inativarPedido
```

Pré-requisitos do cliente no banco local: `idExternoOmie` e `idTabelaPrecoOmie` preenchidos.

---

### 5. Gerador de pedidos (Gerente)

Via `GeradorPedidoBean` — vários pedidos de clientes diferentes na mesma data de produção.

```
Monta lista de Pedido (um por cliente)
  → PedidoService.inserirGeradorPedido(lista)
  → para cada pedido: buildPedidoDTO + pedidoDTO.admin = true
  → OmiePedidoService.cadastrarPedido
```

**`admin = true`** desativa validações comerciais: feriado, fim de semana, horário limite e mínimo de 36 pacotes.

Alteração em lote: `alterarGeradorPedido()`.

---

### 6. Sugestão de pedido (AtivMob)

Integração com plataforma de marketing **AtivMob**.

**Opção A — Pull por CNPJ**

```
POST /ativmob/sugestoesPedido/{cnpj}
  → AtivMobService.proccessSugestoesPedido
  → GET AtivMob /orders/delivery/get_events?storeCNPJ=
  → filtra event_code = "sugestao"
  → grava SugestaoPedido + Itens no PostgreSQL
  → POST ack_events (marca eventos como lidos)
```

**Opção B — Push (webhook)**

```
POST /ativmob/cadastrar-evento (EventRequestDTO)
  → processarSugestaoPedidoRequest
  → grava eventos (dedupe por eventId)
```

**Aprovação manual** (`SugestaoPedidoBean.aprovar`):

1. Valida opção de entrega e quantidades
2. Resolve cliente por `codigoDestino` (id Omie)
3. `PedidoService.inserirSugestaoPedido()` → Omie com `admin=true`
4. `SugestaoPedidoService.aprovar()` → status **Aprovado** no banco local

---

### 7. Relatórios operacionais (Produção e Logística)

Consultam **apenas PostgreSQL** — usados por funcionários/gerentes neste portal.

**Produção do dia** (`ProducaoDiaBean`):

- `ProdutoService.pesquisarProducaoDia(data)`
- JOIN `pedido` + `pedido_produto` + `receita`
- Agrupa quantidades por produto/receita
- Calcula massa total e prioridade
- `ObservacaoProducao` para anotações do dia

**Logística do dia** (`LogisticaDiaBean`):

- `PedidoService.pesquisarLogisticaDia(data, cliente opcional)`
- Agrupa por cliente, opção de entrega e prioridade
- Relatório Jasper: `logistica-dia`

**Produção por período** (`ProducaoPeriodoBean`): mesmo conceito com intervalo de datas.

**Observações de pedido** (`ObservacaoPedidoBean`): observações de produção/logística.

---

### 8. Cadastros e manutenção (portal)

CRUD via `AbstractBean` + services Hibernate → **PostgreSQL**.

| Tela | Bean | Entidade |
|------|------|----------|
| Usuário | `UsuarioBean` | `Usuario` |
| Cliente | `ClienteBean` | `Cliente` |
| Produto | `ProdutoBean` | `Produto` |
| Receita | `ReceitaBean` | `Receita` |
| Tipo | `TipoBean` | `Tipo` |
| Opção entrega | `OpcaoEntregaBean` | `OpcaoEntrega` |
| Feriado | `FeriadoBean` | `Feriado` |
| Sugestão pedido | `SugestaoPedidoBean` | `SugestaoPedido` |
| Avisos | `AvisoBean` | `Aviso` |
| Não conformidades | `NaoConformidadeBean` | `NaoConformidade` |

`ClienteProduto` define quais produtos o cliente pode pedir; preços vêm do **Omie** (tabela de preço do cliente).

---

### 9. Integração Omie

**Clientes**

| Endpoint WS | Função |
|-------------|--------|
| `GET /clientes/idExternoOmie/{id}` | Dados do cliente no Omie |
| `GET /clientes/{nome}` | Busca por nome |
| `PUT /clientes` | Atualização em lote |
| `POST /clientes` | Webhook Omie → sincroniza cadastro local |

**Produtos**

- `OmieProdutoService.listarProdutosPorCliente` — catálogo web e mobile
- `OmieProdutoEstruturaService` — BOM/receita
- `OmieTabelaPrecoService` — tabelas de preço

**Pedidos**

- `IncluirPedido`, `ListarPedidos`, alterar, inativar na API Omie
- Omie é o **sistema de registro** dos pedidos comerciais

---

## Validações de pedido

Aplicadas em `OmiePedidoService` na **criação** (e parcialmente na **alteração**) quando `admin != true`:

| Validação | Regra |
|-----------|--------|
| Cliente ativo | `flgAtivo = N` → bloqueia |
| Duplicidade | Já existe pedido ativo na mesma data (consulta Omie) |
| Feriado | Data em `tb_feriado` ativo |
| Fim de semana | Sábado ou domingo |
| Horário limite | `cliente.horLimite` vs data/hora do pedido |
| Mínimo 36 pacotes | Soma das quantidades ≥ 36 |
| Caracteres | Observação sem acentos/caracteres especiais |

**Exceções por cliente (banco local):**

- `flgEvento = S` → ignora mínimo de pacotes
- `flgMaster = S` → ignora mínimo de pacotes

No web, `PedidoService` também valida **lote mínimo** e **múltiplo** por produto ao adicionar item (exceto cliente evento).

Com `admin = true` (gerador e sugestões aprovadas): feriado, fim de semana, horário e mínimo de pacotes são ignorados.

---

## API REST (speciale-ws)

Base de produção (app mobile): `https://specialepanificacoes.com/spdmws_v2`

Resumo dos recursos:

```
POST   /seguranca/login
POST   /seguranca/recuperarSenha
PUT    /seguranca/alterarSenha

GET    /pedidos?idCliente=&idPedido=&dataPedido=
POST   /pedidos
PUT    /pedidos/{idPedido}
PUT    /pedidos/{id}/inativar

GET    /produtos/clientes/{idCliente}
GET    /produtos/{idProduto}/cliente/{idCliente}
GET    /produtos/estrutura/{idProduto}

GET    /clientes/idExternoOmie/{id}
GET    /clientes/{nomeCliente}
POST   /clientes                    (webhook Omie)

GET    /opcoesEntregas
GET    /opcoesEntregas/{id}/buscarValorFrete/{idCliente}

POST   /ativmob/sugestoesPedido/{cnpj}
POST   /ativmob/cadastrar-evento
```

---

## Ponto de atenção arquitetural

Existe uma **separação implícita** entre:

- **Operação comercial** (pedido do cliente) → **Omie**
- **Planejamento de produção/logística** → **PostgreSQL local**

Se os pedidos criados hoje só são persistidos no Omie e não há job de sincronização no código versionado, os relatórios de produção (`ProducaoDiaBean`, `LogisticaDiaBean`) podem depender de dados históricos ou de um processo externo não presente neste repositório.

**Recomendação:** validar em produção como a tabela `ped.tb_pedido` é alimentada após a migração para Omie.

---

## Stack tecnológica

| Camada | Tecnologias |
|--------|-------------|
| Portal web (este repo) | Java 8, JSF 2.2, RichFaces 4.3, Spring 4.1, Hibernate 4, PostgreSQL, AdminLTE |
| API REST | Java 8, Jersey 1.19, Gson |
| Mobile | Ionic 5, Angular 8, Cordova Android |
| ERP | Omie API |
| Marketing | AtivMob API |
| Build | Maven |

---

## Estrutura deste repositório

```
speciale-web/
├── README.md                 ← este arquivo
└── spdm/
    ├── pom.xml               ← WAR spdm
    └── src/main/
        ├── java/br/com/a2dm/spdm/
        │   ├── bean/         ← Managed Beans JSF
        │   └── config/       ← MenuControl, etc.
        └── webapp/
            ├── pages/        ← Telas .xhtml
            ├── layout/       ← AdminLTE, plugins
            └── WEB-INF/
                └── web.xml
```

---

## Resumo por canal

| Fluxo | Quem usa | Entrada | Persistência | Saída |
|-------|----------|---------|--------------|-------|
| Login web | Todos os perfis | `login.jsf` | Sessão HTTP | Menu por grupo |
| Login mobile | Cliente B2B | App → `/seguranca/login` | localStorage | Loja |
| Novo pedido | Cliente (web/app) | Carrinho / `PedidoBean` | **Omie** | Nº pedido |
| Gerador pedidos | Gerente | `GeradorPedidoBean` | **Omie** (admin) | N pedidos |
| Sugestão AtivMob | Sistema + gerente | AtivMob → WS | Local + **Omie** | Pedido + status |
| Produção/Logística | Funcionário | Data no portal | Leitura **PostgreSQL** | Relatório |
| Webhook cliente | Omie | `POST /clientes` | **PostgreSQL** | ACK |

---

## Projetos relacionados

| Pasta | Artefato | Descrição |
|-------|----------|-----------|
| `../speciale-commum` | `brcmn` | Biblioteca compartilhada A2DM |
| `../speciale-service` | `spdm-service` | Negócio, Omie, app Ionic |
| `../speciale-ws` | `spdmws` | API REST Jersey |
| `../speciale-web` | `spdm` | Portal web (este repo) |
