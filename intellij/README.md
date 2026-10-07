<div align="center">

# Everywhere: DeepSeek Harness for IntelliJ IDEA 🌐⚡

**Conectando o IntelliJ IDEA ao DeepSeek Harness**

[![IntelliJ IDEA 2024.1+](https://img.shields.io/badge/IntelliJ%20IDEA-2024.1.2%20(Community%20%7C%20Ultimate)-red?logo=intellijidea)](https://www.jetbrains.com/idea/)
[![Java Runtime](https://img.shields.io/badge/Java-17-orange?logo=openjdk)](https://openjdk.org/)
[![Embedded Browser](https://img.shields.io/badge/Webview-JCEF%20(Chromium)-teal)]()
[![DeepSeek](https://img.shields.io/badge/AI-DeepSeek--V3%20%7C%20DeepSeek--R1-007acc)](https://deepseek.com)

</div>

Esta é a extensão oficial do **Everywhere** desenvolvida especificamente para o ecossistema JetBrains, com suporte nativo e validado para **IntelliJ IDEA 2024.1.2 (Community Edition / Ultimate, Build #IC-241.17011.79)** e versões posteriores.

Ela incorpora o runtime oficial do **DeepSeek Harness (`dsh`)** em uma janela de ferramentas nativa (**Tool Window**) utilizando **JCEF (Java Chromium Embedded Framework)** acelerado por hardware, oferecendo uma experiência interativa e integrada diretamente ao seu fluxo de desenvolvimento em Java, Kotlin, Spring, Android e projetos Web.

---

## 🌟 Funcionalidades

- 🪟 **Tool Window Nativa com JCEF**: Renderização fluida e moderna com Chromium embutido na barra lateral direita do IntelliJ.
- 📂 **Workspace Project Binding**: Conectado contextualmente à pasta raiz do projeto aberto no IntelliJ IDEA (`project.basePath`).
- 🔄 **Auto-Recuperação Inteligente (Exit Code 1)**: Se houver processos antigos ou portas travadas (3080/3000), o plugin detecta a falha, encerra automaticamente os processos órfãos (`taskkill` / `fuser` / `pkill`) e retoma o serviço em segundos.
- ⚙️ **Página de Configurações Integrada**: Configurações em `Settings -> Tools -> Everywhere: DeepSeek Harness` (chaves de API, portas, perfis, auto-start e caminho do executável).
- 🧭 **Barra de Ações Rápida (Toolbar)**:
  - ▶ **Iniciar**: Inicia o runtime oficial `dsh`.
  - 🛑 **Parar**: Interrompe o processo e desocupa portas.
  - 🔄 **Reiniciar**: Reinicia a sessão rapidamente.
  - 🔁 **Recarregar**: Atualiza a página do webview.
  - 🌐 **Abrir no Navegador**: Abre o link do Harness no navegador padrão do sistema.
  - ⚙️ **Configurações**: Atalho direto para as preferências do plugin.
- 🔔 **Notificações Integradas**: Notificações balão informando quando o serviço fica ativo com a URL local pronta para uso.

---

## 🚀 Como Compilar e Gerar o Pacote do Plugin

### Pré-requisitos
1. **IntelliJ IDEA 2024.1.2** (Community Edition ou Ultimate) ou **JDK 17** instalado.
2. **DeepSeek Harness CLI**:
   ```cmd
   npm install -g @deepseek-ai/dsh
   ```

---

### Opção 1: Compilar via Linha de Comando (Gradle)

Dentro da pasta `intellij`:

#### No Windows (PowerShell / Prompt de Comando):
```cmd
cd intellij
gradlew.bat buildPlugin
```

#### No Linux / macOS:
```bash
cd intellij
./gradlew buildPlugin
```

O arquivo do plugin empacotado será gerado em:
```
intellij/build/distributions/everywhere-intellij-0.1.0.zip
```

---

### Opção 2: Abrir e Compilar Diretamente no IntelliJ IDEA

1. No **IntelliJ IDEA 2024.1.2**:
   - Vá em `File` ➔ `Open...`
   - Selecione a pasta `intellij` deste repositório e confirme a abertura como projeto Gradle.
2. Aguarde a sincronização dos scripts Gradle pelo IntelliJ.
3. Na aba lateral **Gradle** (à direita):
   - Expanda `Tasks` ➔ `intellij` ➔ dê duplo clique em `buildPlugin`.
   - Ou para testar ao vivo em uma instância de desenvolvimento do IntelliJ, execute `runIde`.

---

## 📦 Como Instalar o Plugin no IntelliJ IDEA

1. No IntelliJ IDEA, acesse as preferências:
   - **Windows/Linux**: `File` ➔ `Settings...` (atalho `Ctrl + Alt + S`).
   - **macOS**: `IntelliJ IDEA` ➔ `Settings...` (atalho `Cmd + ,`).
2. No menu à esquerda, selecione **Plugins**.
3. Clique no ícone de engrenagem ⚙️ no topo e escolha:
   **"Install Plugin from Disk..."**
4. Navegue até a pasta `intellij/build/distributions/` e selecione o arquivo:
   `everywhere-intellij-0.1.0.zip`
5. Clique em **OK** e reinicie o IntelliJ IDEA caso solicitado.

---

## 🎯 Como Usar

1. Após a instalação, localize a aba **Everywhere** na barra lateral direita do editor (ou vá no menu superior `Tools` ➔ `Everywhere (DeepSeek Harness)` ➔ `Iniciar DeepSeek Harness`).
2. Se a opção **Auto-Start** estiver habilitada, o DeepSeek Harness iniciará automaticamente na primeira abertura.
3. O agente terá acesso imediato à árvore de diretórios e arquivos do seu projeto.

---

## ⚙️ Configurações (Settings ➔ Tools ➔ Everywhere)

| Opção | Padrão | Descrição |
|---|---|---|
| **Auto Start** | `true` | Iniciar o agente automaticamente ao abrir a Tool Window |
| **Port** | `0` | Porta do servidor web (0 atribui porta livre automaticamente) |
| **Profile** | `"web"` | Perfil de execução do DeepSeek Harness (`web`, `headless`, `tui`) |
| **DeepSeek API Key** | `""` | Chave da API DeepSeek (também aceita via variável `DEEPSEEK_API_KEY`) |
| **Base URL** | `https://api.deepseek.com` | Endpoint base da API DeepSeek |
| **Custom DSH Path** | `""` | Caminho personalizado para o binário `dsh` se não estiver no PATH padrão |
