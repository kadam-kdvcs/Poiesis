# Poiesis 机器音效素材方案

## 推荐素材来源

### Sonniss GDC Game Audio Bundle

- 地址：https://gdc.sonniss.com/
- 适合寻找工业机械、电机、液压、金属运转、蒸汽和电气音效。
- 免费素材可用于游戏及其他媒体项目；使用前仍应保留并阅读对应授权文件。
- 授权说明：https://sonniss.com/gdc-bundle-license/

### Freesound

- 地址：https://freesound.org/
- 适合寻找非常具体的原始录音或机械循环。
- 推荐搜索词：
  - `industrial machine loop`
  - `electric motor hum`
  - `hydraulic press`
  - `factory machinery`
  - `steam valve`
  - `metal mechanism`
  - `conveyor belt`
- 每个素材单独确认授权；优先选择 `CC0`，其次是 `CC-BY`，避免 `CC-BY-NC`。
- 授权说明：https://freesound.org/help/faq/

### Kenney Digital Audio

- 地址：https://www.kenney.nl/assets/digital-audio
- CC0，适合按钮、启动、确认、机械提示音。
- 授权说明：https://kenney.nl/support

### OpenGameArt

- 地址：https://opengameart.org/art-search-advanced?field_art_type_tid%5B0%5D=13
- 有许多 CC0 音效包，适合补充 UI 和简单机械音。

### itch.io CC0 音效

- 地址：https://itch.io/game-assets/assets-cc0/tag-sound-effects
- 可搜索 `industrial`、`machine`、`factory`、`mechanical`。

## Poiesis 的机器音效设计方向

采用多层混音，而不是每台机器只使用一个素材：

1. 底层：低频电机嗡鸣，作为持续工作循环。
2. 中层：齿轮、皮带、液压或泵的节奏。
3. 上层：继电器、阀门、金属碰撞等短音，间隔触发。
4. 完成时：独立的短促机械停止音。

通过改变音调、循环速度、节奏和混音比例，可以让 Separator、Processor、Gasifier、Alloy Factory、Greenhouse 等机器共享素材来源，但拥有不同的声音个性。

## 导入模组前的处理建议

- 循环音剪辑为约 2～5 秒，并处理首尾接缝。
- 统一导出为 OGG。
- 控制循环音音量，避免多台机器同时工作时过响。
- 在资源包中保留第三方素材的许可证和署名文件（如果授权要求署名）。
- “免费”或“免版税”不等于没有使用条件，发布前必须核对每个素材的具体许可。
