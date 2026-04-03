# Trabalho Prático 1 - Jogo da Forca Multijogador
**Sistemas Distribuídos | UBI 2026**

## Autores
* Alexandre Santos (52011)
* Daniel Carlos (52578)
* Vasco Colaço (52290)

---

## Estrutura do Projeto e Organização de Ficheiros

O projeto está organizado em três pacotes principais, seguindo o princípio da **Separação de Responsabilidades** para garantir um código modular e de fácil manutenção.

### Visualização da Árvore de Diretórios
```text
src/pt/ubi/sd/forca/
├── shared/          			# Recursos comuns ao Cliente e Servidor
│   ├── Protocol.java  		# Definição das constantes do protocolo de comunicação
│   └── Config.java    		# Configurações globais (Porto, IP, etc.)
├── server/           			# Lógica do lado do Servidor
│   ├── ServerMain.java   		# Ponto de entrada; aceita novas ligações TCP
│   ├── ClientHandler.java 	# Thread que gere a comunicação individual com cada jogador
│   ├── GameEngine.java    	# Lógica pura do jogo (palavras, máscaras, tentativas)
│   └── GameManager.java   	# Gestor da sessão (lobby, timeouts e sincronização de rondas)
└── client/           			# Lógica do lado do Cliente
    ├── ClientMain.java    	# Ponto de entrada; estabelece ligação com o servidor
    ├── ServerListener.java 	# Thread que ouve mensagens do servidor em tempo real
    └── ConsoleUI.java     	# Interface de utilizador (prints e leitura de teclado)
```
---

## Como Compilar e Executar

> **Nota:** Todos os comandos devem ser executados a partir da **raiz do projeto** (a pasta que contém a subpasta `src`).

### 1. Compilação (Universal)
Para manter o projeto organizado, os ficheiros `.java` da pasta `src` são compilados para uma pasta de saída chamada `bin`.

**No Windows (CMD/PowerShell):**
```
if not exist bin mkdir bin
javac -d bin -sourcepath src src/pt/ubi/sd/forca/server/ServerMain.java src/pt/ubi/sd/forca/client/ClientMain.java src/pt/ubi/sd/forca/shared/*.java
```


**No Linux / macOS (Terminal):**
```
mkdir -p bin
javac -d bin -sourcepath src src/pt/ubi/sd/forca/server/ServerMain.java src/pt/ubi/sd/forca/client/ClientMain.java src/pt/ubi/sd/forca/shared/*.java
```

### 2. Execução do Servidor
O servidor deve ser o primeiro componente a ser iniciado.

**Windows:**
```
java -cp bin pt.ubi.sd.forca.server.ServerMain
```

**Linux / macOS:**
```
java -cp bin pt.ubi.sd.forca.server.ServerMain
```

### 3. Execução do Cliente
Podes abrir múltiplos terminais (entre 2 a 4) para simular diferentes jogadores.

**Windows:**
```
java -cp bin pt.ubi.sd.forca.client.ClientMain
```

**Linux / macOS:**
```
java -cp bin pt.ubi.sd.forca.client.ClientMain
```

## Descrição do Protocolo
O projeto utiliza um protocolo de texto sobre TCP. 
Comandos principais: `WELCOME`, `START`, `GUESS`, `ROUND`, `END`, `FULL`.