package me.myogoo.myotus.integration.ae2;

import me.myogoo.myotus.api.ConfigTab;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Terminal Config 화면의 탭 레지스트리.
 * 외부 모드에서 registerTab()을 호출하여 탭을 추가할 수 있습니다.
 */
public class TerminalConfig {

    private static final List<ConfigTab> TABS = new ArrayList<>();

    /**
     * 새로운 탭을 등록합니다. 등록 순서대로 표시됩니다.
     * 첫 번째 탭(AE2 기본 설정)은 자동으로 추가되므로, 여기서 등록하는 탭은 두 번째부터 표시됩니다.
     *
     * @param tab 등록할 탭 정보
     */
    public static void registerTab(ConfigTab tab) {
        TABS.add(tab);
    }

    /**
     * 등록된 탭 목록을 반환합니다 (읽기 전용).
     */
    public static List<ConfigTab> getTabs() {
        return Collections.unmodifiableList(TABS);
    }
}
