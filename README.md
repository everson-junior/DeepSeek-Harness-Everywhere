<div align="center">

<img src="./assets/everdock-banner.jpg" alt="Everywhere - Conectando suas IDEs ao DeepSeek" width="100%" />

# Everywhere 🌐⚡

**DeepSeek Harness Everywhere — Conectando suas IDEs ao DeepSeek**

[![VS Code Extension](https://img.shields.io/badge/VS%20Code-Extension-blue?logo=visualstudiocode)](./vscode)
[![Visual Studio 2022](https://img.shields.io/badge/Visual%20Studio-2022%20(v17.0%2B)-purple?logo=visualstudio)](./visualstudio)
[![DeepSeek](https://img.shields.io/badge/AI-DeepSeek--V3%20%7C%20DeepSeek--R1-007acc)](https://deepseek.com)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Status](https://img.shields.io/badge/Status-Active%20Development-success)]()

*Everywhere é a ponte definitiva para rodar o DeepSeek Harness em qualquer lugar — trazendo a inteligência autônoma dos modelos DeepSeek diretamente para o seu ambiente de desenvolvimento e fluxo de trabalho diário.*

[Visão Geral](#-visão-geral) •
[Recursos](#-recursos-principais) •
[Extensão VS Code](#-extensão-vs-code) •
[Extensão Visual Studio 2022](#-extensão-visual-studio-2022) •
[Instalação e Uso](#-instalação-e-uso) •
[Configurações](#-configurações) •
[Estrutura do Projeto](#-estrutura-do-projeto)

---

</div>

## 📖 Visão Geral

O **Everywhere** transforma o seu editor em uma central completa para agentes inteligentes do **DeepSeek**. O nome **Everywhere** reflete a flexibilidade de poder rodar o assistente em qualquer lugar: ele automatiza a instalação, inicialização e gerenciamento do runtime oficial do **DeepSeek Harness (`dsh`)**, integrando-o nativamente ao seu workspace.

Com isso, os modelos **DeepSeek-V3** e **DeepSeek-R1 (Reasoner)** ganham acesso supervisionado ao contexto real do projeto: edição e inspeção de código, execução no terminal integrado, histórico contínuo de sessões e ferramentas autônomas sem a necessidade de configurações complexas.

---

## ✨ Recursos Principais

- 🌐 **DeepSeek em Qualquer Lugar**: Gerenciamento e instalação transparente do runtime `dsh`, permitindo o uso ágil em qualquer projeto.
- 🤖 **Agente Autônomo Embutido**: Painel Webview de alta performance na barra lateral e em abas dedicadas do editor (`Editor Tab`).
- 🔄 **Auto-Recuperação & Resiliência**: Tratamento automático para portas ocupadas e encerramento de processos órfãos — reconexão transparente caso ocorra falha inicial de conexão (`exit code 1`).
- 📂 **Contexto Real do Workspace**: O agente roda vinculado à pasta do seu projeto aberto, garantindo precisão em comandos de terminal e edição de código.
- 🛡️ **Proxy de Sessão Seguro**: Handshake de autenticação automático via cookies assinados e neutralização de restrições de iframe (`CSP`/`X-Frame-Options`).
- ⚡ **Gerenciamento de Ciclo de Vida**: Início rápido, reinicialização e parada direta de processos com logs em tempo real no canal de saída.
- ⚙️ **Suporte a Múltiplos Perfis**: Compatível com perfis do DeepSeek Harness (`web`, `headless`, `tui`) e chaves personalizadas de API.

---

## 💻 Extensão VS Code

A extensão oficial para **Visual Studio Code** está localizada no diretório [`/vscode`](./vscode).

### Funcionalidades da Interface

| Local | Ação | Descrição |
|---|---|---|
| **Activity Bar** | Ícone Everywhere / DeepSeek | Abre a interface de chat na barra lateral |
| **Menu Superior** | Iniciar / Parar / Reiniciar | Controle manual do serviço em execução |
| **Barra de Status** | Indicador de Status | Exibe se o agente está ativo, porta em uso e atalhos rápidos |
| **Abas do Editor** | Abrir em Aba Dedicada | Expande a visualização para trabalhar em tela cheia |

---

## 🪟 Extensão Visual Studio 2022

A extensão oficial para **Microsoft Visual Studio 2022** (IDE clássica x64) está localizada no diretório [`/visualstudio`](./visualstudio).

### Características
- **Tool Window Nativa com Microsoft Edge WebView2 + WPF**: Renderização fluida, moderna e com aceleração gráfica da interface do DeepSeek Harness.
- **Solution Binding Automático**: Roda contextualmente vinculado à pasta da Solution aberta no Visual Studio.
- **Auto-Recuperação (Exit Code 1)**: Mata processos órfãos e portas presas e reconecta automaticamente.
- **Menu Integrado**: Disponível em `View` ➔ `Other Windows` ➔ `Everywhere (DeepSeek Harness)`.
- **Configuração no Visual Studio**: Opções em `Tools` ➔ `Options` ➔ `Everywhere`.

---

## 🚀 Instalação e Uso

### Pré-requisitos

1. **Node.js** (v18+) e **npm** ou **pnpm**.
2. **DeepSeek Harness** (`@deepseek-ai/dsh`):
   ```bash
   npm install -g @deepseek-ai/dsh
   ```
   *(A extensão também oferece instalação automática caso não encontre o pacote)*.

### Compilando a Extensão

```bash
# Entre na pasta da extensão
cd vscode

# Instale as dependências
npm install

# Compile o bundle com esbuild
npm run build

# Empacote no formato .vsix (opcional)
node scripts/package.mjs
```

### Executando em Desenvolvimento

Abra a pasta `vscode` no seu editor e pressione <kbd>F5</kbd> para iniciar a janela de depuração da extensão (**Extension Development Host**).

---

## ⚙️ Configurações

Você pode personalizar o comportamento do Everywhere nas configurações do VS Code (`settings.json`):

```json
{
  "deepseek.autoStart": true,
  "deepseek.profile": "web",
  "deepseek.port": 0,
  "deepseek.apiKey": "sua-chave-api-aqui",
  "deepseek.baseUrl": "https://api.deepseek.com",
  "deepseek.dshPath": ""
}
```

| Configuração | Padrão | Descrição |
|---|---|---|
| `deepseek.autoStart` | `false` | Iniciar o agente automaticamente ao abrir o editor |
| `deepseek.port` | `0` | Porta do servidor web (0 atribui porta livre automaticamente) |
| `deepseek.profile` | `"web"` | Perfil de inicialização do DeepSeek Harness |
| `deepseek.apiKey` | `""` | Chave da API DeepSeek (também aceita via `DEEPSEEK_API_KEY`) |
| `deepseek.baseUrl` | `"https://api.deepseek.com"` | Endpoint da API do DeepSeek |
| `deepseek.dshPath` | `""` | Caminho manual ou comando personalizado para o executável `dsh` |

---

## 📁 Estrutura do Projeto

```text
everywhere/
├── .github/
│   └── workflows/
│       └── release.yml         # CI/CD: Geração de .vsix e publicação no Marketplace
├── assets/
│   ├── icon/                   # Ícone oficial em alta resolução (PNG / JPG)
│   └── everdock-banner.jpg     # Banner oficial da extensão
├── visualstudio/               # Extensão para Microsoft Visual Studio 2022 (C# / WPF / VSIX)
│   ├── Everywhere.sln          # Solution do Visual Studio 2022
│   ├── Everywhere/
│   │   ├── Everywhere.csproj   # Projeto C# SDK-style (.NET Framework 4.8)
│   │   ├── EverywherePackage.cs# AsyncPackage principal
│   │   ├── EverywherePackage.vsct # Tabela de comandos e menus
│   │   ├── source.extension.vsixmanifest # Manifesto VSIX v3 para VS 2022 x64
│   │   ├── ToolWindows/        # Tool Window com Microsoft Edge WebView2
│   │   ├── Services/           # DshManager (auto-retry exit code 1 e limpeza de processos)
│   │   ├── Options/            # Página de configurações em Tools -> Options
│   │   └── Resources/          # Ícone e Preview VSIX
│   └── README.md               # Documentação da extensão Visual Studio 2022
├── vscode/                     # Extensão para Visual Studio Code (TypeScript / Webview)
│   ├── assets/
│   │   └── icon/               # Ícone empacotado da extensão
│   ├── media/
│   │   ├── deepseek.svg        # Ícones da interface
│   │   └── everdock-banner.jpg # Banner da extensão
│   ├── src/
│   │   ├── chatViewProvider.ts # Provedor de Webview e UI lateral
│   │   ├── dshManager.ts       # Gerenciador de ciclo de vida e processos
│   │   ├── extension.ts        # Ponto de entrada e comandos VS Code
│   │   └── types.ts            # Definições de tipos e interfaces
│   ├── scripts/
│   │   ├── build.mjs           # Script de bundling esbuild
│   │   └── package.mjs         # Empacotamento VSIX
│   └── package.json            # Manifesto da extensão
└── README.md                   # Documentação principal do projeto
```

---

## 🚀 CI/CD & Publicação no Marketplace

O repositório possui automação via **GitHub Actions** ([`.github/workflows/release.yml`](.github/workflows/release.yml)):

- **Ao publicar uma Release no GitHub**:
  1. O bundle é compilado e o pacote `.vsix` é gerado automaticamente.
  2. O arquivo `.vsix` é anexado diretamente como asset da Release no GitHub.
  3. Se o secret `VS_MARKETPLACE_TOKEN` estiver configurado, a extensão é publicada automaticamente no **Visual Studio Marketplace**.

> **Nota para configuração futura do Marketplace**:
> Para ativar a publicação automática quando for lançar, basta criar um Personal Access Token (PAT) no [Visual Studio Marketplace Management Portal](https://marketplace.visualstudio.com/manage) e adicioná-lo em:  
> `GitHub Repository -> Settings -> Secrets and variables -> Actions -> New repository secret: VS_MARKETPLACE_TOKEN`.

---

## 📄 Licença

Este projeto é distribuído sob os termos da licença MIT.
