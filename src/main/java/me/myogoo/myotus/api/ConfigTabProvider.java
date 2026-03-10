package me.myogoo.myotus.api;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.WidgetContainer;

/**
 * 탭이 선택되었을 때 위젯을 구성하는 콜백 인터페이스.
 */
@FunctionalInterface
public interface ConfigTabProvider {
    /**
     * 탭이 선택되면 호출되어 위젯을 배치합니다.
     *
     * @param widgets 위젯 컨테이너
     * @param screen  현재 화면
     */
    void buildTab(WidgetContainer widgets, AEBaseScreen<?> screen);
}
