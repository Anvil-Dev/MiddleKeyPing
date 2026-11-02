# 标点渲染

世界内标点每帧统一绘制一次，并采用本帧世界画面的最终投影矩阵，使标点位置跟随视野角、行走晃动和受伤晃动。隐藏游戏界面时不显示标点，退出世界时清理投影数据。


## Aleeve Atlas 联动

本分支对应 Minecraft 1.21.1 和 Aleeve Atlas `dev/1.21.1`。安装 Atlas 后通过内嵌 AnvilLib Integration 自动接入，世界内及世界地图的中键短按发送标点、长按使用 MiddleKeyPing 轮盘；地图选区拖动保留原操作。Atlas 内置世界标点输入和覆层关闭，标点同时显示在世界、小地图和世界地图。实体标点跟随已加载的实体，卸载后保留最后坐标至过期；切换维度、退出世界或数量超限会同步清理地图标点。

Atlas 只作为可选编译依赖，不会内嵌。使用最新源码构建与测试：`./gradlew.bat compileJava test runData build --console=plain -PaleeveAtlasDir=<Atlas源码绝对路径>`。

英文语言文件由 `runData` 生成；中文在 `src/main/resources/assets/middle_key_ping/lang/zh_cn.json` 手动维护，配置分组翻译键与 NeoForge 按配置文件名生成的键一致。
