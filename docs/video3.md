## 11. Casos de Uso para Demonstração no Vídeo ✅

O vídeo de demonstração deve incluir os seguintes casos de uso para validar a arquitetura de microserviços:

### CU1: Fluxo Completo de Pagamento → Atribuição de Entregador (E2E Kafka)
**Objetivo:** Demonstrar que um pagamento de pedido dispara automaticamente a atribuição de entregador via eventos Kafka.

**Passos:**
1. Abrir UI Web (http://localhost via Nginx)
2. Fazer login como cliente
3. Selecionar restaurante e produtos
4. Proceder ao pagamento (confirmar pagamento)
5. Observar que:
   - Monólito publica evento `pedido.pago` no Kafka
   - Microserviço consome e cria `Entrega` na sua BD separada
   - Microserviço publica evento `entrega.atribuida`
   - Monólito consome e atualiza `Pedido.status = IN_DELIVERY`
   - UI Web mostra pedido com estado "Em Entrega" com nome do entregador

**Validação visual:** após pagar, o estado do pedido muda de "Pago" para "Em Entrega" automaticamente.

---

### CU2: Atualização de Estado de Entrega (A Caminho → Entregue)
**Objetivo:** Demonstrar que o microserviço consegue atualizar o estado de entrega e propagar essa atualização ao monólito via Kafka.

**Passos:**
1. No pedido em estado "Em Entrega" (do CU1), clicar em "Ver Entrega"
2. Iniciar entrega (estado muda para "A Caminho")
3. Concluir entrega (estado muda para "Entregue")
4. Observar que:
   - Microserviço atualiza `Entrega.status` na sua BD
   - Microserviço publica evento `entrega.status.atualizada`
   - Monólito consome e atualiza projeção local
   - UI Web mostra o novo estado em tempo real

**Validação visual:** a sequência de estados (ATRIBUIDA → A_CAMINHO → CONCLUIDA) está visível na UI.

---

### CU3: Isolamento de Bases de Dados
**Objetivo:** Demonstrar que o microserviço tem BD exclusiva, separada do monólito.

**Passos (via terminal):**
1. Listar tabelas na BD do monólito (`pgserver`):
   ```bash
   docker exec pgserver psql -U user -d postgres -c "\dt"
   ```
   **Esperado:** tabelas como `pedido`, `restaurante`, `produto`, `cliente`, etc.

2. Listar tabelas na BD do microserviço (`entrega-db`):
   ```bash
   docker exec entrega-db psql -U entrega_user -d entrega_db -c "\dt"
   ```
   **Esperado:** apenas `entrega` e `entregador` (nenhuma tabela do monólito).

3. Verificar que dados de entrega criados no passo CU1 existem apenas em `entrega-db`:
   ```bash
   docker exec entrega-db psql -U entrega_user -d entrega_db -c "SELECT * FROM entrega ORDER BY id DESC LIMIT 1;"
   ```

**Validação visual:** as BDs são completamente isoladas, zero sobreposição.

---

### CU4: Proxy Reverso Nginx (REST + gRPC)
**Objetivo:** Demonstrar que o Nginx funciona como Reverse Proxy para REST e gRPC.

**Passos:**
1. **REST via Nginx (porta 80):**
   - Abrir http://localhost/login (sem especificar porta → usa default 80)
   - Verificar que a página carrega (Nginx fez proxy para monólito:8082)

2. **gRPC via Nginx (porta 9090):**
   - Iniciar aplicação JavaFX com `start-dev.bat` (usa `localhost:9090` para gRPC)
   - Verificar que o login funciona e as listagens carregam
   - Confirmar logs que mostram conexão via Nginx (não directo para 9092)

**Validação visual:** Web funciona em localhost:80 (standard HTTP), JavaFX em localhost:9090 (gRPC via Nginx).

---

### CU5: Gestão de Entregadores (CRUD)
**Objetivo:** Demonstrar que o microserviço consegue gerir entregadores de forma independente.

**Passos (via terminal ou UI admin):**
1. Listar entregadores:
   ```bash
   docker exec entrega-service curl -s http://localhost:8081/api/entregadores | jq
   ```
   **Esperado:** listagem com entregadores seed (Bruno, Ana, etc.).

2. Filtrar por disponibilidade:
   ```bash
   docker exec entrega-service curl -s "http://localhost:8081/api/entregadores?disponivel=true" | jq
   ```

3. Atualizar disponibilidade de um entregador:
   ```bash
   docker exec entrega-service curl -s -X PATCH "http://localhost:8081/api/entregadores/1/disponibilidade?disponivel=false" -w " HTTP %{http_code}"
   ```

**Validação visual:** CRUD de entregadores funciona completamente no microserviço.

---

### CU6: Consistência Eventual com Timeout/Retry (Opcional — Advanced)
**Objetivo:** Demonstrar comportamento quando Kafka está momentaneamente indisponível.

**Passos:**
1. Parar o Kafka:
   ```bash
   docker stop kafka
   ```

2. Tentar pagar um pedido na UI Web (deve falhar graciosamente ou fila-se no monólito)

3. Reiniciar Kafka:
   ```bash
   docker start kafka
   ```

4. Verificar que o evento é reprocessado e a entrega é criada

**Validação visual:** sistema aguarda Kafka e recupera graciosamente (sem travamento).

---

### Resumo de Demonstração Recomendado (Duração: ~5-10 minutos)

**Ordem sugerida para o vídeo:**
1. **CU3** (1 min): Mostrar BDs isoladas (terminal `docker exec` comandos)
2. **CU4** (1 min): Mostrar Nginx em ação (browser localhost:80 + JavaFX localhost:9090)
3. **CU1** (3 min): Fluxo E2E completo (login → pagar → estado muda, Kafka logs)
4. **CU2** (2 min): Atualizar estados de entrega
5. **CU5** (1 min): CRUD de entregadores (terminal curl)
6. **CU6** (optional, 1-2 min): Resiliência Kafka (se tempo permitir)

**Dicas para gravação:**
- Usar split screen: browser Web + terminal (`docker logs`) lado a lado
- Narrar claramente o caminho: "Clico em Pagar → Kafka recebe evento → Microserviço processa → BD atualiza → UI refresha"
- Mostrar pelo menos 2 pedidos pagos para validar repetibilidade
- Mencionar explicitamente o isolamento das BDs (CU3)

---

## 11. Casos de Uso para Demonstração no Vídeo ✅

O vídeo de demonstração deve incluir os seguintes casos de uso para validar a arquitetura de microserviços:

### CU1: Fluxo Completo de Pagamento → Atribuição de Entregador (E2E Kafka)
**Objetivo:** Demonstrar que um pagamento de pedido dispara automaticamente a atribuição de entregador via eventos Kafka.

**Passos:**
1. Abrir UI Web (http://localhost via Nginx)
2. Fazer login como cliente
3. Selecionar restaurante e produtos
4. Proceder ao pagamento (confirmar pagamento)
5. Observar que:
   - Monólito publica evento `pedido.pago` no Kafka
   - Microserviço consome e cria `Entrega` na sua BD separada
   - Microserviço publica evento `entrega.atribuida`
   - Monólito consome e atualiza `Pedido.status = IN_DELIVERY`
   - UI Web mostra pedido com estado "Em Entrega" com nome do entregador

**Validação visual:** após pagar, o estado do pedido muda de "Pago" para "Em Entrega" automaticamente.

---

### CU2: Atualização de Estado de Entrega (A Caminho → Entregue)
**Objetivo:** Demonstrar que o microserviço consegue atualizar o estado de entrega e propagar essa atualização ao monólito via Kafka.

**Passos:**
1. No pedido em estado "Em Entrega" (do CU1), clicar em "Ver Entrega"
2. Iniciar entrega (estado muda para "A Caminho")
3. Concluir entrega (estado muda para "Entregue")
4. Observar que:
   - Microserviço atualiza `Entrega.status` na sua BD
   - Microserviço publica evento `entrega.status.atualizada`
   - Monólito consome e atualiza projeção local
   - UI Web mostra o novo estado em tempo real

**Validação visual:** a sequência de estados (ATRIBUIDA → A_CAMINHO → CONCLUIDA) está visível na UI.

---

### CU3: Isolamento de Bases de Dados
**Objetivo:** Demonstrar que o microserviço tem BD exclusiva, separada do monólito.

**Passos (via terminal):**
1. Listar tabelas na BD do monólito (`pgserver`):
   ```bash
   docker exec pgserver psql -U user -d postgres -c "\dt"
   ```
   **Esperado:** tabelas como `pedido`, `restaurante`, `produto`, `cliente`, etc.

2. Listar tabelas na BD do microserviço (`entrega-db`):
   ```bash
   docker exec entrega-db psql -U entrega_user -d entrega_db -c "\dt"
   ```
   **Esperado:** apenas `entrega` e `entregador` (nenhuma tabela do monólito).

3. Verificar que dados de entrega criados no passo CU1 existem apenas em `entrega-db`:
   ```bash
   docker exec entrega-db psql -U entrega_user -d entrega_db -c "SELECT * FROM entrega ORDER BY id DESC LIMIT 1;"
   ```

**Validação visual:** as BDs são completamente isoladas, zero sobreposição.

---

### CU4: Proxy Reverso Nginx (REST + gRPC)
**Objetivo:** Demonstrar que o Nginx funciona como Reverse Proxy para REST e gRPC.

**Passos:**
1. **REST via Nginx (porta 80):**
   - Abrir http://localhost/login (sem especificar porta → usa default 80)
   - Verificar que a página carrega (Nginx fez proxy para monólito:8082)

2. **gRPC via Nginx (porta 9090):**
   - Iniciar aplicação JavaFX com `start-dev.bat` (usa `localhost:9090` para gRPC)
   - Verificar que o login funciona e as listagens carregam
   - Confirmar logs que mostram conexão via Nginx (não directo para 9092)

**Validação visual:** Web funciona em localhost:80 (standard HTTP), JavaFX em localhost:9090 (gRPC via Nginx).

---

### CU5: Gestão de Entregadores (CRUD)
**Objetivo:** Demonstrar que o microserviço consegue gerir entregadores de forma independente.

**Passos (via terminal ou UI admin):**
1. Listar entregadores:
   ```bash
   docker exec entrega-service curl -s http://localhost:8081/api/entregadores | jq
   ```
   **Esperado:** listagem com entregadores seed (Bruno, Ana, etc.).

2. Filtrar por disponibilidade:
   ```bash
   docker exec entrega-service curl -s "http://localhost:8081/api/entregadores?disponivel=true" | jq
   ```

3. Atualizar disponibilidade de um entregador:
   ```bash
   docker exec entrega-service curl -s -X PATCH "http://localhost:8081/api/entregadores/1/disponibilidade?disponivel=false" -w " HTTP %{http_code}"
   ```

**Validação visual:** CRUD de entregadores funciona completamente no microserviço.

---

### CU6: Consistência Eventual com Timeout/Retry (Opcional — Advanced)
**Objetivo:** Demonstrar comportamento quando Kafka está momentaneamente indisponível.

**Passos:**
1. Parar o Kafka:
   ```bash
   docker stop kafka
   ```

2. Tentar pagar um pedido na UI Web (deve falhar graciosamente ou fila-se no monólito)

3. Reiniciar Kafka:
   ```bash
   docker start kafka
   ```

4. Verificar que o evento é reprocessado e a entrega é criada

**Validação visual:** sistema aguarda Kafka e recupera graciosamente (sem travamento).

---

### Resumo de Demonstração Recomendado (Duração: ~5-10 minutos)

**Ordem sugerida para o vídeo:**
1. **CU3** (1 min): Mostrar BDs isoladas (terminal `docker exec` comandos)
2. **CU4** (1 min): Mostrar Nginx em ação (browser localhost:80 + JavaFX localhost:9090)
3. **CU1** (3 min): Fluxo E2E completo (login → pagar → estado muda, Kafka logs)
4. **CU2** (2 min): Atualizar estados de entrega
5. **CU5** (1 min): CRUD de entregadores (terminal curl)
6. **CU6** (optional, 1-2 min): Resilência Kafka (se tempo permitir)

**Dicas para gravação:**
- Usar split screen: browser Web + terminal (`docker logs`) lado a lado
- Narrar claramente o caminho: "Clico em Pagar → Kafka recebe evento → Microserviço processa → BD atualiza → UI refresha"
- Mostrar pelo menos 2 pedidos pagos para validar repetibilidade
- Mencionar explicitamente o isolamento das BDs (CU3)