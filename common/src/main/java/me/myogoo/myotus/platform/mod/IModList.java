package me.myogoo.myotus.platform.mod;

import me.myogoo.myotus.dto.MyoModInfo;

public interface IModList {
    IModList EMPTY = new IModList() {
        @Override
        public boolean isLoaded(String modId) {
            return false;
        }

        @Override
        public MyoModInfo getModInfoById(String modId) {
            return null;
        }
    };

    boolean isLoaded(String modId);

    MyoModInfo getModInfoById(String modId);
}
