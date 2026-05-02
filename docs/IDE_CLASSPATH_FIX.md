# Resolução de Erros "Cannot be resolved to a type" no VS Code

## Problema:
VS Code mostra erros como:
- `TascaEatsServiceGrpc cannot be resolved to a type`
- `CriarMenuRequest cannot be resolved to a type`
- `AvaliacaoResponse cannot be resolved to a type`

Mas `mvn clean compile` passa com sucesso (**BUILD SUCCESS**).

## Causa:
O VS Code (IDE) não refrescou o classpath com os ficheiros gerados pelo protobuf compiler. Os ficheiros **existem** em `target/generated-sources/protobuf/`, mas o IDE não os está a reconhecer.

## Solução (Escolha uma):

### **Opção 1: Refrescar Java Project (Mais Rápido) ⭐ RECOMENDADO**

1. Abrir Command Palette: `Ctrl+Shift+P`
2. Digitar: `Java: Clean Language Server Workspace`
3. Pressionar Enter
4. Fechar e reabrir o VS Code
5. Os erros devem desaparecer automaticamente

### **Opção 2: Refrescar Maven**

1. Command Palette: `Ctrl+Shift+P`
2. Digitar: `Maven: Update project`
3. Selecionar o projecto
4. Deixar compilar completamente
5. Reabrir ficheiros Java

### **Opção 3: Remover Pasta .classpath (Nuclear)**

1. Abrir terminal no VS Code: `Ctrl+` (backtick)
2. Executar:
```bash
Remove-Item ".vscode" -Recurse -Force
# Fechar VS Code completamente
# Reabrir VS Code
```
3. VS Code vai regenerar toda a configuração

### **Opção 4: Rebuild de Linha de Comando**

```bash
.\mvnw.cmd clean install -DskipTests
```

Depois de qualquer uma das opções acima, os erros de IDE devem desaparecer. A compilação já está 100% correcta!

## Verificar Status:

Para confirmar que está tudo bem, executar:
```bash
.\mvnw.cmd clean compile 2>&1 | Select-String "BUILD SUCCESS"
```

Se disser `[INFO] BUILD SUCCESS`, então **tudo está correcto** (só o IDE precisa refrescar).

## Ficheiros Alterados:

- `.vscode/settings.json` — Atualizado para usar `updateBuildConfiguration: automatic`
- Removido: `src/main/java/.../TascaEatsGrpcService.java` (ficheiro antigo/duplicado)

---

**Nota**: Os erros do IDE são apenas avisos visuais. O compilador Maven compila sem problemas. Isto é comum com geração de código.
