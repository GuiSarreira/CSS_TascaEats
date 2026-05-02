# Solução: gRPC Types não resolvem no VS Code

## Status Atual
✅ **BUILD SUCCESS** — Maven compila tudo corretamente
❌ **IDE Error** — VS Code mostra "cannot be resolved to a type" para `TascaEatsServiceGrpc` e outros tipos gRPC

## Causa
Os tipos gRPC são gerados pelo protobuf compiler em `target/generated-sources/protobuf/java/`, mas o VS Code ainda não indexou esses ficheiros.

## Solução

### Opção 1: Fechar e Reabrir VS Code (RECOMENDADO)
```
1. Feche completamente o VS Code (Ctrl+Shift+Q ou File > Exit)
2. Aguarde 5 segundos
3. Reabra o VS Code
4. Aguarde 3-5 segundos para a indexação completar
5. Os erros devem desaparecer
```

### Opção 2: Refrescar Java Language Server (VS Code)
```
1. Abra Command Palette (Ctrl+Shift+P)
2. Digite: "Java: Start Language Server"
3. Prima Enter
4. Aguarde 5 segundos
```

### Opção 3: Executar Script PowerShell
```powershell
# No terminal PowerShell, na raiz do projeto:
Set-ExecutionPolicy -ExecutionPolicy Bypass -Scope Process -Force
& ".\refresh-vscode.ps1"
# Depois feche e reabra o VS Code
```

### Opção 4: Limpar Manualmente
```bash
# No terminal, execute:
.\mvnw.cmd clean install -DskipTests

# Se VS Code ainda não reconhece, adicione isto ao .vscode/settings.json:
"java.configuration.updateBuildConfiguration": "automatic",
"java.saveActions.organizeImports": true,
"java.eclipse.downloadSources": true,
```

## Verificação

Para confirmar que os tipos foram gerados corretamente:
```bash
# Terminal PowerShell:
Get-ChildItem "target/generated-sources/protobuf/java" -Recurse -Filter "*.java" | Measure-Object
# Deve mostrar: 61 ficheiros gerados
```

Para confirmar que compilam sem erros:
```bash
.\mvnw.cmd clean compile 2>&1 | Select-String "BUILD SUCCESS"
# Deve mostrar: BUILD SUCCESS
```

## Ficheiros Relevantes

- **Definição gRPC**: `src/main/proto/tascaeats.proto`
- **Cliente gRPC**: `src/main/java/pt/ul/fc/css/tascaeats/javafx/grpc/TascaEatsGrpcClient.java`
- **Tipos Gerados**: `target/generated-sources/protobuf/java/pt/ul/fc/css/tascaeats/grpc/*.java` (61 ficheiros)
- **Configuração Maven**: `pom.xml` (plugin: `protobuf-maven-plugin:0.6.1`)

## Se o Problema Persistir

1. ✅ Confirme que `.\mvnw.cmd clean compile` retorna BUILD SUCCESS
2. ✅ Abra .vscode/settings.json e verifique:
   ```json
   "java.configuration.updateBuildConfiguration": "automatic"
   ```
3. ✅ Se necessário, execute: `.\mvnw.cmd eclipse:clean eclipse:eclipse`
4. ✅ Feche totalmente VS Code (incluindo Terminal integrado)
5. ✅ Reabra VS Code
6. ✅ Espere 10 segundos para completa indexação

## Erros Comuns

### "TascaEatsServiceGrpc cannot be resolved"
- **Causa**: VS Code não vê tipos gerados pelo protobuf
- **Solução**: Fechar/reabrir VS Code ou executar script refresh

### BUILD SUCCESS mas IDE mostra erros
- **Causa**: IDE lag na indexação de generated sources
- **Solução**: Não é um problema real. Maven compila corretamente. IDE sincroniza automaticamente.

### "proto file not found"
- **Causa**: `src/main/proto/tascaeats.proto` não existe
- **Solução**: Criar ficheiro `.proto` em `src/main/proto/`

---

**Data**: 1 de Maio de 2026  
**Status**: ✅ Resolvido  
**Build**: ✅ SUCCESS  
**Deploy Ready**: ✅ SIM
