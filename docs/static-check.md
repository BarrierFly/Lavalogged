# Lavalogged 多版本移植静态检查报告

对照 [guardian](../../guardian)（Minecraft 反编译源码库）的四个版本分支逐一核对
`lavalogged` / `waterlogged` 属性相关代码。核对方法：

1. 解析每个版本 `net.minecraft(.world.level).block` 包内全部类声明，构建继承图；
2. 计算水登录接口（yarn `Waterloggable` / mojmap `SimpleWaterloggedBlock`）的**直接实现类**
   与**传递闭包**（所有含水子类）；
3. 对每个闭包成员检查是否在**自身源码**中声明目标方法（`onUse` / `useItemOn` /
   `getStateForNeighborUpdate` / `getPlacementState` 等）——mixin 只能注入自己声明的方法；
4. 逐一核对各版本 API 签名与字段结构（见下文"机制核对"）。

## 版本矩阵

| 版本 | mappings | Java | 属性后端 | waterlogged 直接实现类数 |
| --- | --- | --- | --- | --- |
| 1.19.4 | yarn `1.19.4+build.2` | 17 | Statement 4.2.9（单 jar 覆盖 1.14.4–1.20.4） | 33 |
| 1.20.1 | yarn `1.20.1+build.10` | 17 | Statement 4.2.9 | 34（+DecoratedPotBlock） |
| 1.21.11 | 官方映射（共用 26.3 源码） | 21 | 模组自带 `LavaStateRefresher` | 39 |
| 26.3 | 无混淆（`fabric.loom.disableObfuscation=true`） | 21 | 模组自带 `LavaStateRefresher` | 39（SpeleothemBlock 换名） |

## 上游代码的问题与修正

上游（2024-09 快照，目标 1.20.4）的类清单与"所有含水方块都可 lavalog"的设计意图不完全一致。
逐项核对结果：

### 已修正（按版本）

| 问题 | 说明 | 修正 |
| --- | --- | --- |
| `GrateBlock` / `BarrierBlock` 在 1.19.4 / 1.20.1 不可含水 | Barrier 自 1.21 起才有 waterlogged；Grate 自 1.21 才存在 | 这两个版本从 `Lavaloggable` 中移除（1.19.4 同时移除 1.20 才有的 `DecoratedPotBlock`） |
| `BigDripleafStemBlock` 遗漏 | 所有版本均为含水直接实现类，上游清单缺失 | 四个版本全部加入 |
| `LightBlock`、`StairsBlock`、`HangingSignBlock`、`WallHangingSignBlock` 遗漏于桶交互 | 它们各自声明 `onUse`（1.21.11 为 `onUseWithItem`），不注入时基类拦截被覆写，方块无法用岩浆桶/桶交互 | 按版本加入 `BucketUseMixin` |
| `SculkShriekerBlock`、`AbstractSignBlock`、`CoralBlock` 遗漏于邻居更新 | 均声明 `getStateForNeighborUpdate`，缺失时对应方块 lavalog 后岩浆不更新流动 | 按版本加入 `NeighborUpdateMixin` |
| `CalibratedSculkSensorBlock` 遗漏于放置 | 1.20.1+ 声明 `getPlacementState` | 1.20.1 加入 |
| `CoralBlockBlock` 等 | 其继承自 `Block` 而非 `CoralParentBlock`，不可含水 | 不加入（确认上游未加入是对的） |
| `BlockMixin` 为死代码 | 其注入的 `Block#setDefaultState` 在属性添加前运行，守卫恒假；Statement 的重建流程也不依赖它 | 移除整个文件与 mixins.json 条目 |
| 上游未声明 Statement 依赖 | `fabric.mod.json` 缺 `"statement"` depends，运行时会缺库崩溃 | 1.19.4 / 1.20.1 补上；1.21.11 / 26.3 无此依赖 |

### 1.21.11 / 26.3 的新清单要点

* 珊瑚：yarn `CoralParentBlock` → mojmap `BaseCoralPlantTypeBlock`；
* 亚麻石/发光地衣：`GlowLichenBlock` → `MultifaceBlock`（`GlowLichenBlock`、
  `MultifaceGrowthBlock`、`SculkVeinBlock` 均为其子类，由父类覆盖）；
* 1.21+ 新增含水方块：`BarrierBlock`、`GrateBlock`/`WaterloggedTransparentBlock`（26.3 改名）、
  `CopperGolemStatueBlock`、`DriedGhastBlock`、`HeavyCoreBlock`、`ShelfBlock`；
* 26.3 改名：`PointedDripstoneBlock` → `SpeleothemBlock`、`PropaguleBlock` → `MangrovePropaguleBlock`、
  `StairsBlock` → `StairBlock`、`TrapdoorBlock` → `TrapDoorBlock`、`HorizontalConnectingBlock` → `CrossCollisionBlock`；
* `CrafterBlock` / `VaultBlock`：1.21.11 / 26.3 均无 waterlogged 属性（1.21.11 方块状态
  JSON 与类声明都已确认），不加入。

## 机制核对（1.21.11 / 26.3 自带属性注入）

上游依赖 Statement 追加属性；其官方构建止步 1.20.4。1.21.11 / 26.3 由模组自带
`org.lavalogged.state.LavaStateRefresher` 完成，核心步骤与逐版本差异如下：

| 环节 | 1.21.11（mojmap） | 26.3 |
| --- | --- | --- |
| 值存储 | `StateHolder.values: Reference2ObjectArrayMap`（可变 map，直接 put） | `propertyKeys[] / propertyValues[]` 并行数组（重建数组替换，`@Mutable`） |
| 转移表 | `neighbours: Map<Property<?>, S[]>`，置 null 后 `populateNeighbours(...)` 重建 | `neighbors: S[][]` 矩阵，按 `getPossibleValues()` 序号手工重建 |
| codec | 每状态一个 `propertiesCodec`，逐状态替换 | 移至 `StateDefinition.propertiesCodec`，在定义上替换 |
| 新状态构造 | `factory.create(owner, map, codec)` | `factory.create(owner, keys[], values[])` |
| 注册 | 新状态按注册表顺序追加进 `Block.BLOCK_STATE_REGISTRY`（mojmap 名，yarn 为 `STATE_IDS`）；`getId() == -1` 才 add，**既有 id 全部保持不变** | 同左 |

关键正确性要点（均已在实现中落实）：

1. **id 稳定性**：只追加新状态，原状态对象原样保留 → 客户端/服务端只要按同一注册表顺序
   执行就得到一致 id；连接原版服务器时原版 id 前缀不受影响；旧存档不受影响。
2. **默认状态对象不变**：新状态表 = 旧状态列表 + 新变体，`states.get(0)` 仍是原默认状态，
   `Block.defaultBlockState` 字段无需改动。
3. **`initCache()` 必须手动调用**：mojmap 两个版本的 `BlockStateBase.fluidState` 都是在
   `initCache()`（vanilla 于 `Blocks` 类初始化时调用）里缓存的，新状态不调用则
   `getFluidState()` 永远返回空 → 不调用 `AbstractBlockMixin.getFluidState` 的注入就看不到岩浆。
   Statement 老版本对应 `statement_initShapeCache`，1.19.4 / 1.20.1 由 Statement 自己处理。
4. **codec 重建**：经 `@Invoker appendPropertyCodec` 链式重建，使 `/data`、世界生成 JSON、
   NBT 磁盘路径能解析 `lavalogged` 字段；NBT 读写本身（`NbtHelper.toBlockState/fromBlockState`）
   是动态遍历 state manager 属性的，属性加进 manager 后天然兼容。
5. **方块状态 JSON 无需改动**：现代版本的 blockstate 变体键只包含影响模型用的属性
   （如 `acacia_slab.json` 只写 `type`），`lavalogged=true` 的状态按其余属性命中同一模型，
   渲染与原版一致（岩浆由流体渲染层绘制）。
6. **网络交互**：26.3 起挥动手臂由 `InteractionResult.SUCCESS`（`SwingSource`）驱动，
   不再手动 `swing`；流体 tick 名为 `getTickDelay(LevelReader)`（yarn 为 `getTickRate`）；
   `Fluids.LAVA.defaultFluidState()` 替代 yarn 的 `getStill(false)`。

## 与 guardian 分支的对照方式

```
guardian（yarn/vineflower 分支）：  be0c9cb421 = 1.19.4   44b34e1b42 = 1.20.1   a67a152b87 = 1.21.11
guardian（mojmap/vineflower 分支）：192e7132b4 = 1.21.11   d5822ca2a1 = 26.3
```

每个版本独立跑"直接实现 + 传递闭包 + 方法声明矩阵"三份脚本，交叉验证后生成最终清单。
1.19.4 源码里已含 1.20 实验内容（樱花树、饰纹罐、悬挂木牌等类），但 `DecoratedPotBlock`
在 1.19.4 未实现 `Waterloggable`，仍按 1.19.4 实际含水关系处理。

## 构建结果

```
./gradlew :1.19.4:build :1.20.1:build :1.21.11:build :26.3:build
```

| 产物 | 类 | Statement 依赖 | mixins 配置 |
| --- | --- | --- | --- |
| `versions/1.19.4/build/libs/Lavalogged-1.0.0+1.19.4.jar` | 6 | depends 声明 | 1 个（5 个 mixin） |
| `versions/1.20.1/build/libs/Lavalogged-1.0.0+1.20.1.jar` | 6 | depends 声明 | 1 个（5 个 mixin） |
| `versions/1.21.11/build/libs/Lavalogged-1.0.0+1.21.11.jar` | 11 | 无 | 2 个（+状态注入 2 mixin） |
| `versions/26.3/build/libs/Lavalogged-1.0.0+26.3.jar` | 11 | 无 | 2 个（+状态注入 2 mixin） |

四个版本的 stonecutter 指令展开均已抽查（1.19.4 正确剔除 `DecoratedPotBlock`、
26.3 正确切换 `SpeleothemBlock` 与并行数组实现）。mixin 运行期应用情况需在游戏内验证。

# 机制完善轮（对照 LavaloggingV1 与 vanilla 源码）

第二轮按用户确认的规则把熔岩机制补全。所有规则先经用户确认：

## 充熔岩（四条路径的门控）

| 路径 | 门控 | 内部流体 |
| --- | --- | --- |
| 桶充（`useItemOn`/`onUse` 拦截 + vanilla `FluidFillable`/`LiquidBlockContainer` 接口路径） | `isLavaFillable`：未充、未含水、碰撞形状非完整方块 | 熔岩源 |
| 先放熔岩后放方块（`getPlacementState`/`getStateForPlacement`） | 同上 + 位置流体必须是**熔岩源**（vanilla 台阶对水同款：流动中放置从不充入） | 熔岩源 |
| 流动熔岩流入（被检测） | vanilla `spreadTo` → `placeLiquid`：默认实现只接受**源**流体。流动熔岩只有在 `getNewLiquid` 把它提升为源时才会进入——该提升要求 `gamerule lavaSourceConversion` 开启 + 目标位置周围 ≥2 相邻源 + 下方固体或源。即"无限熔岩规则控制源向可含熔岩方块填充"，与原版无限水规则控制充水机制完全一致 | 熔岩源 |
| 发射器（免费获得） | 与桶同路径 | 熔岩源 |

实现方式：`Lavaloggable` 接口在 `Waterloggable`/`SimpleWaterloggedBlock` 的默认方法基础上
增加熔岩分支（水逻辑经 `Waterloggable.super`/`SimpleWaterloggedBlock.super` 保留），
水与熔岩互斥（已含熔岩拒绝水，反之亦然）。

## 收走与破坏（两相保留）

* 收走熔岩：`tryDrainFluid`/`pickupBlock` 返回熔岩桶并把方块置回 `lavalogged=false`，
  若方块因此失去支撑则按 vanilla 语义破坏；桶/发射器路径自动生效。
* 破坏方块：`AbstractBlockStateMixin` 在状态级钩子（yarn `onStateReplaced` /
  mojmap `affectNeighborsAfterRemoval`——选状态级是因为箱子等子类覆写块级方法会遮蔽注入）
  检测"旧状态 lavalogged + 新状态为空气 + 非活塞移动"，在原位留一个熔岩源。

## 发光

`AbstractBlockStateMixin` 注入 `getLuminance`（yarn）/`getLightEmission`（mojmap），
lavalogged 状态发光 15（与真实熔岩一致；光照本身按整格位置计算，"形状考虑"由充入门控承担——
无空间容纳熔岩的状态不可能被充入，自然不发光）。

## 方块形状（两处）

1. **形状缓存修复**：这些类在构造器里按"当时的全部状态"建形状表，运行后追加的
   lavalogged 状态查表得 null → 崩溃。修法 = 覆写形状方法、按 `with(lavalogged=false)` 查表：
   * 1.19.4 / 1.20.1：`WallBlock`（outline+collision 两张表）、`BigDripleafBlock`（outline；
     collision 按 tilt 静态取，无需修）、`MultifaceGrowthBlock`（outline）；
   * 1.21.11 / 26.3：`WallBlock`、`BigDripleafBlock`、`MultifaceBlock`、
     `CrossCollisionBlock`（后两者均为 outline+collision）。
2. **流体流入/流出的形状判定**：vanilla 的 `canPassThroughWall` 比对源/目标碰撞形状、
   `isSolidFace` 检查面强度——完全复用 vanilla，无需自写。

## 桶交互补充

* 保留对会吞掉交互的方块（箱子、末影箱、木牌家族、`ShelfBlock`、`CopperGolemStatueBlock`、
  `DecoratedPotBlock`、营火、蜡烛等）的 `onUse`/`useItemOn` 拦截——vanilla 桶路径在这些方块上
  会先被方块自身的交互逻辑吃掉；
* 非交互方块的桶充/收走走 vanilla 接口路径（同时覆盖发射器）；
* 收取音效：vanilla 的 `getBucketFillSound`/`getPickupSound` 无状态参数，用线程本地标记
  （`LavaDrainSound`）在 `tryDrainFluid`/`pickupBlock` 与声音回调之间传递"刚收的是熔岩"。

## 未采用（参考 V1 后的决定）

* V1 的数据包名单、铁砧/下落方块扩展——用户选择维持含水清单；
* V1 的 `BucketItem.use` 整体拦截——用接口默认实现 + 少量块级拦截覆盖面更完整（含发射器）；
* V1 对双层台阶的特判——推广为"碰撞形状为完整方块的状态禁止充入"通用规则。

