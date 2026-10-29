package dev.anvilcraft.ping.client;

import dev.anvilcraft.ping.util.Ping;
import net.minecraft.network.chat.Component;

import java.util.Collection;

/// 外部地图标点集成，所有方法均在客户端主线程调用。
public interface PingIntegration {
    /// 初始化地图输入和世界标点显示。
    void initialize();

    /// 同步收到的标点和发送者。
    void add(Ping ping, Component sender);

    /// 更新标点位置与生命周期。
    void tick();

    /// 获取当前维度需要在世界内渲染的标点。
    Collection<Ping> visiblePings();

    /// 清理当前世界的标点。
    void clear();
}
