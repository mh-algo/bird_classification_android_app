package com.earlybird.catchbird.data

class UploadChk() {
    companion object {
        // 사진 데이터 업로드 여부를 체크
        // false일 경우 업로드 이전
        // true일 경우 업로드 후를 나타냄
        // camerafragment에 진입할 경우 false로 초기화
        // 서버에 사진 업로드 할 경우 true로 설정
        var chk = false
    }
}
