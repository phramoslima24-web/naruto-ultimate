# Narutoto Ultimate RPG — Fase 1 (base)

Addon independente para **Minecraft 1.20.1 + Forge 47.x**, criado para integrar os addons do ecossistema Narutoto sem alterar os JARs originais.

## O que já está implementado

- Mod Forge independente: `narutoto_ultimate`.
- Dependência obrigatória do Narutoto base `0.4.2` ou superior.
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
6. Coloque esse JAR em `mods` junto com `narutoto-0.4.2-forge-1.20.1.jar` e Forge 1.20.1.

## Requisitos

- Minecraft 1.20.1
- Forge 47.x
- Narutoto base 0.4.2 ou superior
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
