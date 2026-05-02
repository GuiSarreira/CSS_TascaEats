# 🧪 Testing Fase G — JavaFX Quick Guide

**Last Updated**: 1 de Maio de 2026

---

## ✅ Build Status Check

```bash
# Verify no compile errors
.\mvnw.cmd clean compile
# Expected: [INFO] BUILD SUCCESS ✅

# Verify JAR created
Get-ChildItem target/tascaeats-1.0.jar
# Expected: ~60MB file exists ✅

# Verify gRPC stubs generated
Get-ChildItem target/generated-sources/protobuf/java -Recurse *.java | Measure-Object
# Expected: 61 files ✅
```

---

## 🚀 Test 1: Start Application

### Method A: Maven (Recommended for testing)
```bash
cd C:\Users\guisa\Documentos\GitHub\CSS_TascaEats

# Start backend + frontend
.\mvnw.cmd spring-boot:run

# Expected output:
# - Spring Boot starts on port 8080 (REST)
# - gRPC Server starts on port 9090
# - JavaFX window opens after 2-3 seconds
# - Login screen appears
```

### Method B: Direct JAR (After first build)
```bash
# Build first
.\mvnw.cmd clean install -DskipTests

# Then run
$env:JAVA_HOME = "C:\Program Files\Java\jdk-24"
java -jar target/tascaeats-1.0.jar

# Same behavior as Method A
```

---

## 🔐 Test 2: Login Screen

### Expected Behavior
```
1. Window title: "TascaEats — Login"
2. Size: 500x600
3. Logo: "TascaEats" (orange/bold)
4. Two input fields pre-filled:
   - Email: user@example.com
   - Password: password123
5. Two buttons: "Entrar" (red/orange), "Registar" (gray)
```

### Test Flow
```
✓ Click "Entrar" with correct credentials
  → Should load main.fxml (MenuBar + Restaurantes)
  → Window resizes to 1000x800
  → Title changes to "TascaEats — Sistema de Gestão"

✓ Try wrong credentials
  → Should show error in red box: "Email ou palavra-passe incorretos"
  
✓ Try empty fields
  → Should show: "Por favor, preencha todos os campos"
```

---

## 🏪 Test 3: Restaurantes View (Pessoa 2)

### Expected Components
```
Title: "Restaurantes Disponíveis"

Button Bar:
  - [Recarregar] — Fetches from gRPC
  - [+ Novo Restaurante] — TODO placeholder
  - [🛒 Ver Carrinho] — TODO placeholder

TableView with columns:
  - ID | Nome | Localização | Avaliação | Status | Ações

Status Bar:
  - "Aguardando..." initially
  - "✅ N restaurantes carregados" after load
```

### Test Flow
```
1. RestaurantesController auto-initializes
   → Should see loading message
   → After 2-3 seconds: tableView populates with restaurants

2. Check TableView data
   → Should have columns: ID, Nome, Localização, Avaliação (5.0), Status
   → Should have rows with actual data from gRPC

3. Click [Recarregar]
   → Should say "Carregando restaurantes..."
   → Button disables
   → After 2-3s: data refreshes, button enables

4. Check Ações buttons in each row
   → [Ver Menus] button → placeholder alert
   → [Avaliar] button → placeholder alert
```

### Troubleshooting

**Issue**: "Aguardando..." never changes
- gRPC not running: Start backend first
- Wrong port: Check application.properties (should be 9090)
- Firewall: Allow localhost connections

**Issue**: TableView empty after load
- Check Spring Boot logs for gRPC errors
- Verify database has restaurante data

**Issue**: Exception in console
- Look for "[RestaurantesController] Error:"
- Check network connectivity to localhost:9090

---

## 📋 Test 4: Menus View (Pessoa 2)

### How to Access
```
Menu Bar → Menus → "Menus por Restaurante"
```

### Expected Components
```
Filter Bar:
  - "Restaurante:" [ComboBox] [Recarregar] [+ Novo Menu]

TableView:
  - Columns: ID | Nome Menu | Descrição | Preço | Restaurante | Ações
  
Status Bar:
  - "Total: X menus"
```

### Test Flow
```
1. View loads
   → ComboBox should auto-populate with restaurants
   → First restaurant auto-selected
   → TableView should populate with menus for that restaurant

2. Change restaurant in ComboBox
   → TableView refreshes with new menus
   → Total count updates

3. Click [+ Novo Menu]
   → Alert: "Funcionalidade em desenvolvimento..." (TODO)

4. Click Ações buttons
   → [Editar] → alert TODO
   → [Adicionar] → alert "adicionado ao carrinho"
   → [Remover] → confirmation dialog
```

---

## ⭐ Test 5: Avaliações View (Pessoa 1)

### How to Access
```
Menu Bar → Avaliações → "Minhas Avaliações"
```

### Expected Components
```
Button Bar:
  - [Recarregar] [+ Nova Avaliação]

TableView:
  - Columns: ID | Menu | Restaurante | Classificação | Comentário | Data | Ações
  
Status Bar:
  - "Total: X avaliações"
```

### Test Flow
```
1. View loads
   → Should load user's avaliações from gRPC
   → TableView populates with data

2. Check data columns
   → Menu: "Restaurante: [name]" (shows restaurante name)
   → Classificação: numeric (1-5)
   → Data: formatted as "dd/MM/yyyy HH:mm"

3. Click [Recarregar]
   → Refreshes from gRPC

4. Click Ações buttons
   → [Editar] → placeholder alert
   → [Remover] → confirmation dialog
     → OK: removes via gRPC, refreshes table
     → Cancel: closes dialog
```

---

## 🔄 Test 6: Menu Bar Navigation

### All Options
```
Restaurantes
  ├─ [Listar Restaurantes] → loads RestaurantesController ✅
  └─ [Sair] → exits application ✅

Menus  
  ├─ [Menus por Restaurante] → loads MenusController ✅
  └─ [Novo Menu] → loads MenusController ✅

Avaliações
  ├─ [Minhas Avaliações] → loads AvaliacoesController ✅
  └─ [Nova Avaliação] → loads AvaliacoesController ✅

Conta
  ├─ [Perfil] → alert "TODO"
  ├─ [Definições] → alert "TODO"
  └─ [Logout] → alert "TODO"

Ajuda
  └─ [Sobre] → alert "TODO"
```

### Test Flow
```
1. Click each menu item
   → Correct view should load
   → TableView should populate with data
   → Status messages should show loading/complete
```

---

## 🐛 Common Issues & Solutions

| Issue | Cause | Solution |
|-------|-------|----------|
| Login screen doesn't appear | Spring Boot not started | Wait 3-5 seconds for Spring to initialize |
| Restaurantes shows no data | gRPC server not running | Check port 9090 is open |
| TableView empty after "load" | Database empty | Check database has data in `restaurante` table |
| Application freezes | Blocking gRPC call | Wait 5-10 seconds, should complete |
| Console shows "Connection refused" | Wrong port or host | Verify application.properties: grpc.server.port=9090 |
| FXML loading error | File not found | Check `/fxml/` files exist in resources |
| Null pointer in controller | gRPC connection fails | Check Spring Boot logs for errors |

---

## 📊 Performance Expectations

| Operation | Expected Time | Indicator |
|-----------|---------------|-----------|
| Application startup | 3-5 seconds | Spring Boot initializing |
| Login screen appearance | 2-3 seconds | After startup complete |
| List Restaurantes | 1-2 seconds | Thread + gRPC call |
| Filter Menus | 1-2 seconds | Thread + gRPC call |
| TableView render | <100ms | After data received |
| Click button → alert | <100ms | Instant UI feedback |

---

## ✅ Success Criteria

### Minimum Viable Test (5 minutes)
```
✓ Application starts without errors
✓ Login screen appears
✓ Can click "Entrar" with demo credentials
✓ MainController and MenuBar appear
✓ RestaurantesController loads and shows data
✓ Can click menu items without crashes
```

### Comprehensive Test (15 minutes)
```
✓ All of Minimum Viable Test
✓ All TableViews populate with data
✓ Can navigate between all views
✓ Action buttons don't crash (show placeholders)
✓ Status labels update correctly
✓ No exceptions in console
✓ Application closes cleanly
```

---

## 📝 Logging

### Enable Debug Output
```java
// Already enabled in each controller:
System.out.println("[RestaurantesController] Carregando...");
System.err.println("[RestaurantesController] Error: " + e.getMessage());
```

### Watch Console for:
```
✅ [TascaEatsFXApp] Iniciando aplicação JavaFX...
✅ [LoginController] Inicializando...
✅ [MainController] Inicializando...
✅ [RestaurantesController] Inicializando...
✅ [RestaurantesController] Carregando restaurantes...
✅ [gRPC] Conectado ao servidor: localhost:9090

❌ [RestaurantesController] Error: 
❌ [gRPC ERROR] 
❌ Exception: 
```

---

## 🎯 Next Steps After Testing

### If Everything Works ✅
1. Proceed with form implementation (Create/Update/Delete)
2. Convert to AsyncStub for true non-blocking
3. Add CSS styling
4. Implement remaining TODO features

### If Issues Found ❌
1. Check console for specific error messages
2. Verify Spring Boot started correctly
3. Check gRPC server is running on port 9090
4. Verify database has sample data
5. Review [GRPC_TYPES_FIX.md](GRPC_TYPES_FIX.md) for IDE issues

---

**Ready to Test? Run:**
```bash
.\mvnw.cmd spring-boot:run
```

✅ Good luck! Report any issues found.
