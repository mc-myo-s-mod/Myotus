package me.myogoo.myotus.platform.mod;

import me.myogoo.myotus.dto.MyoModInfoDto;

public interface IModList {
    IModList EMPTY = new IModList() {
        @Override
        public boolean isLoaded(String modId) {
            return false;
        }

        @Override
        public MyoModInfoDto getModInfoById(String modId) {
            return null;
        }
    };

    boolean isLoaded(String modId);

    MyoModInfoDto getModInfoById(String modId);
}
