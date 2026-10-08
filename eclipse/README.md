<div align="center">

# Everywhere: DeepSeek Harness for Eclipse IDE 🌐⚡

**Conectando o Eclipse IDE ao DeepSeek Harness**

[![Eclipse IDE](https://img.shields.io/badge/Eclipse%20IDE-2026--09%20(4.41.0)-2c2255?logo=eclipseide)](https://eclipse.org/)
[![Java Runtime](https://img.shields.io/badge/Java-17%2B%20%7C%2025-orange?logo=openjdk)](https://openjdk.org/)
[![Embedded Browser](https://img.shields.io/badge/SWT%20Browser-WebKitGTK%20%7C%20Edge%20%7C%20WebKit-blue)]()
[![DeepSeek](https://img.shields.io/badge/AI-DeepSeek--V3%20%7C%20DeepSeek--R1-007acc)](https://deepseek.com)
[![Status](https://img.shields.io/badge/Status-Ready-success)]()

</div>

Esta é a extensão oficial do **Everywhere** desenvolvida para o **Eclipse IDE**, com suporte nativo e validado para **Eclipse IDE for Java Developers (2026-09 / 4.41.0, Build id: 20260903-0720)** e versões compatíveis.

Ela incorpora o runtime oficial do **DeepSeek Harness (`dsh`)** em uma View nativa do Eclipse utilizando o widget **SWT Browser**, conectando os modelos autônomos **DeepSeek-V3** e **DeepSeek-R1** diretamente ao seu workspace de desenvolvimento.

---

## 🌟 Funcionalidades

- 🪟 **View Nativa com Navegador Embutido**: Exibe a interface do DeepSeek Harness diretamente em uma aba/view do Eclipse (`Window -> Show View -> Other... -> Everywhere`).
- 📂 **Workspace & Project Binding**: O assistente é iniciado com o diretório do projeto ou workspace atualmente ativo (`IWorkspace` / `IProject`), permitindo inspeção, criação e edição de código contextual.
- 🔄 **Auto-Recuperação Inteligente (Exit Code 1)**: Detecção automática de conflito de portas (3080/3000) ou encerramento inesperado, finalizando processos órfãos (`fuser`, `pkill`, `taskkill`) e reiniciando a conexão de forma transparente.
- 📄 **Console de Logs Integrado**: Canal de log em tempo real na aba padrão do Eclipse (`Console` ➔ `Everywhere - DeepSeek Harness`) com formatação e diferenciação por cores de saída e erro.
- 🧭 **Barra de Ferramentas Dedicada (View Toolbar)**:
  - ▶ **Iniciar**: Inicia o runtime `dsh`.
  - 🛑 **Parar**: Interrompe o processo e libera portas de rede.
  - 🔄 **Reiniciar**: Reinicia a sessão do assistente.
  - 🔁 **Recarregar**: Atualiza a página do navegador embutido.
  - 🌐 **Abrir no Navegador**: Abre a interface web no browser padrão do sistema operacional.
  - ⚙️ **Configurações**: Acesso direto às preferências do plugin.
- ⚙️ **Página de Preferências do Eclipse**: Integrada ao menu padrão `Window` ➔ `Preferences` ➔ `Everywhere (DeepSeek)`.
- ⌨️ **Menu e Atalhos no Workbench**: Menu `Everywhere` na barra principal com comandos para iniciar, parar e abrir a visão.

---

## 🚀 Como Compilar e Empacotar o Plugin

### Pré-requisitos
1. **Eclipse IDE 2026-09 (4.41.0)** ou qualquer instalação Eclipse compatível.
2. **Java 17+** ou **Java 25** (o compilador bundled do Eclipse é detectado automaticamente).
3. **DeepSeek Harness CLI**:
   ```bash
   npm install -g @deepseek-ai/dsh
   ```

---

### Compilação e Instalação Rápida

Dentro do diretório `eclipse`:

```bash
# Dar permissão de execução (se necessário)
chmod +x build.sh

# Compilar e instalar diretamente no Eclipse local
./build.sh --install
```

O script irá:
1. Detectar o compilador Java (bundled no Eclipse ou JDK do sistema).
2. Localizar as bibliotecas do Eclipse (`org.eclipse.ui`, `swt`, `core.runtime`, etc.).
3. Compilar os arquivos fontes em `eclipse/src/`.
4. Gerar o pacote OSGi `eclipse/build/com.deepseek.everywhere_1.0.0.jar`.
5. Instalar nas pastas `dropins` e `plugins` da sua instalação Eclipse, registrando no `bundles.info`.

---

### Instalação Manual

Se preferir copiar manualmente o arquivo `.jar`:

1. Gere o pacote executando `./build.sh`.
2. Copie o arquivo gerado:
   ```bash
   cp eclipse/build/com.deepseek.everywhere_1.0.0.jar <caminho_do_eclipse>/dropins/
   ```
   *(No Snap do Ubuntu, o diretório é: `~/snap/eclipse/common/eclipse/dropins/`)*
3. Inicie ou reinicie o Eclipse com o parâmetro `-clean` se necessário:
   ```bash
   eclipse -clean
   ```

---

## 🎯 Como Usar no Eclipse

1. Abra o **Eclipse IDE 2026-09**.
2. Abra a visão Everywhere:
   - Menu `Window` ➔ `Show View` ➔ `Other...`
   - Expanda a categoria **Everywhere** e selecione **Everywhere (DeepSeek Harness)**.
   - Ou clique no ícone do DeepSeek na barra de ferramentas superior.
3. Se a opção **Auto Start** estiver habilitada, o DeepSeek Harness iniciará automaticamente.
4. Caso esteja parado, clique no botão **▶ Iniciar DeepSeek Harness** na tela inicial ou na barra de ações.
5. Para acompanhar diagnósticos, abra a visualização **Console** e selecione o console `Everywhere - DeepSeek Harness`.

---

## ⚙️ Configurações (Window ➔ Preferences ➔ Everywhere)

| Opção | Padrão | Descrição |
|---|---|---|
| **Iniciar automaticamente** | `true` | Inicia o runtime automaticamente ao abrir a visão |
| **Porta do Servidor HTTP** | `0` | Porta local (0 atribui porta livre automaticamente) |
| **Perfil do Harness** | `"web"` | Perfil de inicialização do DeepSeek Harness (`web`, `headless`, `tui`) |
| **DeepSeek API Key** | `""` | Chave de API DeepSeek (também herdada de `DEEPSEEK_API_KEY`) |
| **URL Base da API** | `https://api.deepseek.com` | Endpoint da API DeepSeek |
| **Caminho customizado 'dsh'** | `""` | Caminho personalizado para o binário `dsh` caso não esteja no PATH |

---

## 🏛️ Estrutura do Código

```
eclipse/
├── .classpath                   # Configuração de classpath do Eclipse PDE
├── .project                     # Configuração de projeto do Eclipse
├── build.properties             # Descritor de build PDE
├── build.sh                     # Script de compilação e auto-instalação
├── pom.xml                      # Configuração para Maven Tycho
├── plugin.xml                   # Registro de Views, Menus, Commands e Preferences
├── META-INF/
│   └── MANIFEST.MF              # Manifesto OSGi com dependências do Eclipse
├── icons/                       # Ícones de ações e janelas (PNG e SVG)
└── src/com/deepseek/everywhere/
    ├── Activator.java           # Ciclo de vida do bundle e limpeza ao fechar o Eclipse
    ├── actions/                 # Ações da barra de ferramentas (Start, Stop, Restart, etc.)
    ├── console/                 # Integração com MessageConsole do Eclipse
    ├── handlers/                # Manipuladores de comandos do Workbench
    ├── model/                   # Enums de estado e interfaces de ouvintes
    ├── preferences/             # Páginas e inicializadores de configurações
    ├── service/                 # Gerenciador de ciclo de vida e resiliência de processos (DshManager)
    └── ui/                      # Visualização SWT com navegador embutido (EverywhereView)
```
