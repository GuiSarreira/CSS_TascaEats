=== INÍCIO DO VÍDEO ===

0 - CLONE + DOCKER COMPOSE UP
[0]
- git clone <repo_url>
- cd CSS_TascaEats
- docker compose up --build
- Aguardar 7 serviços: pgserver, entrega-db, zookeeper, kafka, springbootapp, entrega-service, nginx
- Quando logs mostrarem "[DataInitializer] Inicialização completa" → pronto

NGINX - REVERSE PROXY (ARQUITETURA)
[NGINX]
- REST: abrir http://localhost (porta 80) → página de login carrega via Nginx → proxy para springbootapp:8082
- gRPC: abrir nativa com .\mvnw.cmd javafx:run (porta 9090) → login funciona via Nginx → proxy para springbootapp:9092
- Prova: nenhuma interface comunica diretamente com o monólito, tudo passa pelo Nginx

BD - ISOLAMENTO DE BASES DE DADOS (ARQUITETURA)
[BD]
- BD Monólito:
  docker exec pgserver psql -U user -d postgres -c "\dt"
  → tabelas: user_table, pedido, restaurante, produto, pagamento, avaliacao, menu, etc.
- BD Microserviço:
  docker exec entrega-db psql -U entrega_user -d entrega_db -c "\dt"
  → APENAS: entrega, entregador (zero sobreposição com monólito)
- Dados entregadores no microserviço:
  docker exec entrega-db psql -U entrega_user -d entrega_db -c "SELECT id, nome, email, disponivel FROM entregador;"
  → Bruno Silva, Ana Costa, Carlos Matos, Filipa Nunes (todos disponíveis)

A - LOGIN MOCK
[A]
- Web (http://localhost): login com ana@tascaeats.pt (válido, qualquer password).
- Nativa (.\mvnw.cmd javafx:run, porta 9090): login com admin@tascaeats.pt (válido).
- Invalido: tentar email inexistente (ex: naoexiste@tascaeats.pt) → erro.

J - CRIAÇÃO DE PEDIDO (Web via Nginx)
[J]
- Login como ana@tascaeats.pt na web (http://localhost).
- Em "Pedidos" → "+ Novo Pedido", escolher morada guardada ou preencher nova.
- Selecionar Tasca Lisboa: Prego no Pão x2, Sopa do Dia x1 e confirmar.
- Resultado: novo pedido com estado CREATED na lista de pedidos.
- Invalido: produto indisponível (Arroz de Pato — campo bloqueado).

K - PAGAMENTO → KAFKA → ATRIBUIÇÃO AUTOMÁTICA DE ENTREGADOR
[K]
*** DEMONSTRAÇÃO PRINCIPAL DA FASE 3 — FLUXO E2E KAFKA ***
- Nativa: login como admin@tascaeats.pt.
- Ir a Pagamentos, selecionar o pedido CREATED (seed #1 ou o criado em J).
- Pagar: MULTIBANCO, ref: 222 333 444, bandeira: VISA.
- ABRIR TERMINAL PARALELO — OBSERVAR FLUXO KAFKA:
  1) docker logs springbootapp --tail 10
     → "Evento Kafka publicado no tópico 'pedido.pago': {pedidoId:..., morada:...}"
  2) docker logs entrega-service --tail 10
     → "Evento pedido.pago consumido" → "Entrega criada, entregador atribuído automaticamente"
     → "Evento Kafka publicado: entrega.atribuida"
  3) docker logs springbootapp --tail 10
     → "Evento 'entrega.atribuida' consumido, pedido atualizado para IN_DELIVERY"
- VERIFICAR BD DO MICROSERVIÇO (prova que entrega foi criada na BD separada):
  docker exec entrega-db psql -U entrega_user -d entrega_db -c "SELECT id, pedido_id, entregador_id, estado FROM entrega ORDER BY id DESC LIMIT 1;"
  → nova entrega com pedido_id correspondente e estado ATRIBUIDA
- Resultado: estado do pedido muda CREATED → PAID → IN_DELIVERY automaticamente.
- Invalido: tentar pagar pedido já DELIVERED (ex: #5) → erro "estado inválido para pagamento".

L - ATUALIZAÇÃO DO ESTADO DA ENTREGA (via Microserviço + Kafka)
[L]
- Nativa: abrir entrega do pedido acabado de pagar em K.
- Iniciar entrega → estado muda ATRIBUIDA → A_CAMINHO.
- OBSERVAR LOGS:
  docker logs entrega-service --tail 5
  → "entrega.status.atualizada publicado"
  docker logs springbootapp --tail 5
  → "Evento entrega.status.atualizada consumido, projeção atualizada"
- Concluir entrega → estado muda A_CAMINHO → CONCLUIDA.
- VERIFICAR BD MICROSERVIÇO:
  docker exec entrega-db psql -U entrega_user -d entrega_db -c "SELECT id, pedido_id, estado FROM entrega ORDER BY id DESC LIMIT 3;"
  → estado CONCLUIDA confirmado na BD isolada
- Resultado: pedido aparece como DELIVERED nas interfaces (web e nativa).
- Invalido: tentar avançar entrega já CONCLUIDA → erro.

M - GESTÃO DE ENTREGADORES NO MICROSERVIÇO
[M]
- Listar todos os entregadores (BD do microserviço):
  docker exec entrega-db psql -U entrega_user -d entrega_db -c "SELECT id, nome, email, veiculo, zona_atuacao, disponivel FROM entregador;"
  → 4+ entregadores, todos disponíveis inicialmente
- Verificar que entregadores NÃO existem na BD do monólito (isolamento):
  docker exec pgserver psql -U user -d postgres -c "SELECT COUNT(*) FROM entregador;"
  → erro ou 0 (tabela não existe no monólito para entregas)
- Mostrar filtro/busca na nativa (se disponível) ou via terminal.
- Invalido: se todos indisponíveis → atribuição automática fica pendente (consistência eventual).

N - CANCELAMENTO DE PEDIDO
[N]
- Web (ana@tascaeats.pt): na lista de pedidos, cancelar pedido em CREATED → CANCELLED (válido).
- Invalido: tentar cancelar pedido DELIVERED (#5) → ação não permitida.

KAFKA - TÓPICOS (VERIFICAÇÃO OPCIONAL)
[KAFKA]
- Listar tópicos Kafka ativos:
  docker exec kafka kafka-topics --bootstrap-server localhost:9092 --list
  → pedido.pago, entrega.atribuida, entrega.status.atualizada

=== RESUMO DO FLUXO ARQUITETURAL DEMONSTRADO ===
Web/JavaFX → Nginx (porta 80/9090) → Monólito (8082/9092) → Kafka → Microserviço → BD Entrega
                                                                    ↑                      |
                                                                    |   Kafka (resposta)    |
                                                                    ← ← ← ← ← ← ← ← ← ← ←

ORDEM SUGERIDA PARA GRAVAÇÃO:
1. [0]     Clone + docker compose up (~2 min)
2. [NGINX] Provar Nginx: abrir web + abrir nativa (~1 min)
3. [BD]    Mostrar BDs isoladas no terminal (~1 min)
4. [A]     Login válido + inválido (~30s)
5. [J]     Criar pedido via web (~1 min)
6. [K]     Pagar → ver Kafka logs → entrega automática → verificar BD micro (~3 min)
7. [L]     Atualizar estado entrega → ver logs Kafka → DELIVERED (~2 min)
8. [M]     Mostrar entregadores no microserviço (~1 min)
9. [N]     Cancelar pedido válido + inválido (~30s)
10.[KAFKA] Listar tópicos Kafka (~30s)
Total estimado: ~12–15 min