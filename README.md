# Narutoto Ultimate RPG — Fase 1 (base)

Addon independente para **Minecraft 1.20.1 + Forge 47.x**, criado para integrar os addons do ecossistema Narutoto sem alterar os JARs originais.

## O que já está implementado

- Mod Forge independente: `narutoto_ultimate`.
- Dependência obrigatória do Narutoto base `0.4.1` ou superior.
- Detecção opcional dos addons: Clans, All Bijuu, Mangekyou, Akatsuki & ANBU, Ninja Wars, Rinnegan Paths, Rinne-Sharingan, Edo Tensei, Juubi e Narutolimit.
- Perfil persistente por jogador: rank, vila, clã, organização, XP, reputação e missões concluídas.
- Os dados do perfil ficam no NBT persistente do jogador e são copiados no evento de clone/renascimento.
- Comandos administrativos para consultar e editar os dados iniciais.

## Comandos

| Comando | Função |
|---|---|
| `/ultimate profile` | Mostra seu perfil |
| `/ultimate profile <jogador>` | Mostra o perfil de outro jogador (OP) |
| `/ultimate addons` | Lista addons detectados |
| `/ultimate setrank <jogador> <rank>` | Define o rank (OP) |
| `/ultimate setvillage <jogador> <vila>` | Define a vila (OP) |
| `/ultimate setclan <jogador> <cla>` | Define o clã (OP) |
| `/ultimate addxp <jogador> <quantidade>` | Adiciona XP (OP) |
| `/ultimate addrep <jogador> <quantidade>` | Altera reputação (OP; aceita valor negativo) |

Use palavras sem espaço nos argumentos. Exemplo: `/ultimate setvillage Steve KONOHA`.

## Como compilar no Windows

1. Instale **Java 17 (64-bit)**. Minecraft 1.20.1/Forge deve ser compilado com Java 17.
2. Extraia a pasta do projeto.
3. Dê dois cliques em `build-windows.bat`.
4. Na primeira execução, o script baixa Gradle 8.1.1. O Gradle/ForgeGradle também baixará as dependências necessárias, então é preciso ter internet.
5. O JAR será gerado em `build/libs/narutoto-ultimate-0.1.0-alpha.jar`.
6. Coloque esse JAR em `mods` junto com `narutoto-0.4.1-forge-1.20.1.jar` e Forge 1.20.1.

## Requisitos

- Minecraft 1.20.1
- Forge 47.x
- Narutoto base 0.4.1 ou superior
- Java 17 para compilar

Os outros addons são opcionais nesta fase. O projeto detecta sua presença, mas ainda não chama classes internas deles; assim, não exige que todos estejam instalados nem cria dependências de compilação frágeis.

## Próximas fases

1. Sistema de ranks com requisitos e promoção.
2. Missões, diário e recompensas sem duplicar sistemas existentes.
3. Organizações ANBU/Akatsuki/Nukenin com integração protegida por detecção de addon.
4. Integração de clãs, Bijuu, dojutsu e guerras.
5. Livro Shinobi (GUI), histórico e configurações.

## Observações técnicas

- O projeto não modifica nem inclui cópias dos JARs originais.
- O perfil usa uma chave própria (`NarutotoUltimate`) no NBT persistente do jogador.
- Esta é uma primeira base funcional de desenvolvimento, ainda não uma versão final com todos os sistemas RPG.


## Compilar automaticamente pelo GitHub

1. Envie **todos os arquivos e pastas de dentro de `narutoto-ultimate`** para a raiz do seu repositório GitHub.
2. Confirme que a pasta `.github/workflows/build.yml` também foi enviada. No GitHub, arquivos iniciados por ponto podem ficar ocultos no Explorador do Windows; ative **Exibir > Mostrar > Itens ocultos** se precisar.
3. Abra a aba **Actions** do repositório.
4. O workflow `Build Narutoto Ultimate RPG` compila automaticamente quando você enviar um commit para `main` ou `master`. Também pode iniciar manualmente em **Actions > Build Narutoto Ultimate RPG > Run workflow**.
5. Quando terminar com o sinal verde, abra a execução e baixe o artefato **narutoto-ultimate-jar** na seção **Artifacts**. Dentro dele estará o `.jar` compilado.

Se a compilação falhar, abra a execução em **Actions**, copie o primeiro erro vermelho completo e envie aqui.


## Campanha principal (v0.2.0-alpha)

A campanha segue a progressão **Genin -> Chunin -> Jounin -> ANBU -> Kage**.
O progresso fica salvo nos dados persistentes de cada jogador.

Use `/ultimate campaign` para consultar o capítulo e os objetivos atuais.
Monstros hostis derrotados por você concedem 10 XP Ultimate e 1 ponto de reputação.
As promoções são automáticas quando os requisitos são cumpridos:

| Promoção | Requisitos |
|---|---|
| Genin -> Chunin | 5 monstros derrotados e 50 XP Ultimate |
| Chunin -> Jounin | 10 monstros derrotados na etapa e 150 XP acumulados |
| Jounin -> ANBU | 20 monstros derrotados na etapa e 350 XP acumulados |
| ANBU -> Kage | 30 monstros derrotados na etapa, 650 XP acumulados e derrotar Wither ou Ender Dragon |

Ao virar ANBU, a organização do perfil muda para `ANBU`. Ao virar Kage, muda para `KAGE`.


## Sistema de missões e gabinetes dos Kage (v0.3.0-alpha)

A campanha agora avança por missões oficiais entregues ao Kage da vila. O jogador precisa falar com o NPC do gabinete para aceitar uma missão e voltar ao mesmo Kage para entregar a missão concluída.

### Gabinetes das cinco grandes vilas

Os NPCs são aldeões especiais com interação própria:
- KONOHA: Hokage
- SUNA: Kazekage
- KIRI: Mizukage
- IWA: Tsuchikage
- KUMO: Raikage

Um administrador deve ir até o gabinete da vila e executar `/ultimate spawnkage KONOHA` (troque pela vila desejada). O NPC aparece ao lado do administrador; mova-o para dentro do gabinete, se necessário. O addon fornece o NPC e a função de gabinete, mas não gera automaticamente a construção física do prédio.

O primeiro Kage com quem um jogador sem vila conversar define a vila dele. Depois disso, só o Kage da vila correspondente entrega missões para esse jogador.

### Como jogar

1. O administrador cria o NPC do Kage dentro do gabinete da vila com `/ultimate spawnkage KONOHA`.
2. O jogador conversa (botão direito) com o Kage.
3. O Kage apresenta a próxima missão.
4. O jogador usa `/ultimate aceitar`.
5. Derrote os inimigos pedidos. A missão salva o progresso mesmo se o jogador sair do mundo.
6. Volte ao gabinete e clique no Kage novamente.
7. Use `/ultimate entregar` para receber XP, reputação e contar a missão para a promoção.

Comandos:
- `/ultimate missions` - consultar a missão atual e o progresso.
- `/ultimate campaign` - ver a campanha.
- `/ultimate aceitar` - aceitar a missão apresentada pelo Kage.
- `/ultimate entregar` - entregar uma missão concluída ao Kage.
- `/ultimate spawnkage KONOHA` - criar NPC de Konoha (requer OP); também aceita `SUNA`, `KIRI`, `IWA` e `KUMO`.

### Quantidade de missões por rank

| Rank | Missões necessárias para promoção |
|---|---:|
| Genin | 5 |
| Chunin | 10 |
| Jounin | 15 |
| ANBU | 20 |
| Kage | 25 para concluir a campanha final |

As missões ficam progressivamente mais difíceis. A última missão ANBU exige derrotar um chefe lendário (Wither ou Ender Dragon). Nas missões Kage, uma missão de chefe aparece a cada cinco missões. Cada missão entregue dá XP Ultimate e reputação conforme o rank.

**Nota:** as missões desta versão usam monstros hostis do Minecraft como objetivos provisórios. A próxima evolução pode ligar os objetivos a NPCs, inimigos, Bijuu e sistemas específicos dos addons Narutoto.

## Atualização 0.5.0-alpha — Kage e invasões

- `/ultimate spawnkage KONOHA` cria o Minato Namikaze original do Narutoto 0.4.1.
- `/ultimate spawnkage SUNA` cria o Gaara original do Narutoto 0.4.1.
- As outras três vilas continuam usando o NPC genérico do gabinete para preservar o quadro de missões.
- Os Kage são registrados com dados persistentes e podem reagir ao serem atacados.
- Invasões raras usam os Missing Nin do Narutoto; cada grupo tem 5 ou 6 inimigos.
- Os ninjas encontrados nas proximidades, identificados pelas tags de vila do Narutoto, recebem os invasores como alvos.
- O sistema de missões e a campanha existentes foram mantidos.

**Importante:** nesta versão, o comando `/ultimate spawnkage VILA` ainda precisa ser executado por um administrador dentro de cada vila. A geração automática dos prédios dos cinco gabinetes no momento da criação do mundo ainda não está implementada, porque o JAR do Narutoto não expõe uma API pública para localizar com segurança as coordenadas de cada vila já gerada. A invasão só pode acontecer perto de um Kage registrado. Não remova o mod base Narutoto 0.4.1.

### GitHub Actions

O workflow está em `.github/workflows/build.yml`. Envie o conteúdo desta pasta para a raiz do repositório; a compilação inicia ao enviar para `main`/`master` ou manualmente em **Actions → Build Narutoto Ultimate RPG → Run workflow**. Baixe o artefato `narutoto-ultimate-0.5.0` ao terminar.


## Compatibilidade adaptada

Esta edição foi ajustada para declarar dependência do Narutoto base 0.4.1 (Minecraft 1.20.1) e usar Forge 47.4.10 no ambiente de compilação. Os IDs de entidades Minato, Gaara e Missing Nin usados pelo código foram encontrados no JAR Narutoto 0.4.1 fornecido. Ainda é necessário compilar e testar no jogo para confirmar o funcionamento em execução.
