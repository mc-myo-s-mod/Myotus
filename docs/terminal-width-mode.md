# ME 터미널 폭: Compact / Wide

지원하는 ME 터미널의 왼쪽 도구 모음에서 터미널 폭 버튼을 눌러 **Compact**와 **Wide**를 전환한다. 툴팁은 현재 선택된 모드를 표시한다. 선택은 클라이언트 전역 설정으로 저장되어 다른 터미널과 게임 재시작 후에도 유지된다. AE2의 터미널 높이 설정은 별도로 동작한다.

Compact는 원래 스타일을 사용한다. Wide는 해당 화면의 AE2 스타일과 같은 디렉터리에 있는 전용 형제 스타일 `*_wide.json`을 사용한다. 예를 들어 `assets/ae2/screens/terminals/terminal.json`의 Wide 스타일은 `assets/ae2/screens/terminals/terminal_wide.json`이다. 전용 스타일이 없는 터미널은 원래 화면을 사용한다.

## Wide 스타일 작성

리소스 팩 또는 애드온에서 대상 화면의 원래 스타일 이름에 `_wide`를 붙인 JSON을 제공한다. Wide 스타일에 열 수, 배경 조각과 텍스처, 슬롯, 위젯과 텍스트의 위치를 직접 정의한다. Myotus는 원본 그림을 늘이거나 임의로 이어 붙이지 않는다. Wide 그림과 각 위치가 서로 맞도록 제작해야 한다. 작성한 폭은 창 크기에 맞춰 자동으로 줄어들지 않는다. 기존에 등록된 위젯 ID와 슬롯 의미, 숨김 설정을 유지한다. 원래 스타일을 `includes`로 포함하고 달라진 항목을 덮어쓸 수 있다. AE2는 각 위젯이나 슬롯 항목을 통째로 덮어쓰므로 해당 항목의 필요한 속성과 숨김 설정을 빠짐없이 다시 적는다.

다음은 AE2 1.21.1의 `terminals/terminal.json`에 대응하는 `terminals/terminal_wide.json` 예시다. 267×256 텍스처에는 아래 다섯 영역이 모두 그려져 있어야 한다. 검색창과 스크롤바도 넓어진 그림에 맞게 배치하며, 다른 슬롯·위젯·텍스트도 디자인에 맞게 조정한다.

```json
{
  "$schema": "../schema.json",
  "includes": ["terminal.json"],
  "terminalStyle": {
    "slotsPerRow": 13,
    "header": { "texture": "guis/terminal-wide.png", "textureWidth": 267, "textureHeight": 256, "srcRect": [0, 0, 267, 17] },
    "firstRow": { "texture": "guis/terminal-wide.png", "textureWidth": 267, "textureHeight": 256, "srcRect": [0, 17, 267, 18] },
    "row": { "texture": "guis/terminal-wide.png", "textureWidth": 267, "textureHeight": 256, "srcRect": [0, 35, 267, 18] },
    "lastRow": { "texture": "guis/terminal-wide.png", "textureWidth": 267, "textureHeight": 256, "srcRect": [0, 53, 267, 18] },
    "bottom": { "texture": "guis/terminal-wide.png", "textureWidth": 267, "textureHeight": 256, "srcRect": [0, 71, 267, 99] }
  },
  "widgets": {
    "scrollbar": { "left": 247, "top": 18 },
    "search": { "left": 152, "top": 4, "width": 89, "height": 12 }
  }
}
```

`srcRect`는 `[x, y, 너비, 높이]`다. 텍스처 이름은 자유롭게 정할 수 있으며 이미지 파일만 추가해서는 Wide 스타일이 활성화되지 않는다. 화면마다 원본 스타일과 레이아웃이 다르므로 각 대상의 슬롯과 위젯, 텍스트 및 배경을 확인해야 한다.

## 공개 인터페이스

`me.myogoo.myotus.api.client.ITerminalWidthScreen`은 호환성을 위해 `AEBaseScreen`에 계속 구현되며 자식 화면도 상속한다.

```java
if (screen instanceof ITerminalWidthScreen widthScreen) {
    widthScreen.setTerminalWidthMode(TerminalWidthMode.WIDE);
}
```

`getTerminalWidthMode()`는 전역 설정의 선택값을 반환하고 `setTerminalWidthMode()`는 선택값을 저장하고 화면을 갱신한다. Minecraft 클라이언트 스레드에서 호출한다. 인터페이스를 상속한 모든 화면에 Wide 스타일이 있다는 뜻은 아니며, 지원하지 않는 터미널은 원래 레이아웃을 유지한다.
