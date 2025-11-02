package my_app.soar.EntryPage.SignUp

data class PolicyItem(
    val id: Int,
    val title: String,
    val required: Boolean,
    var checked: Boolean = false      // UI 상태 저장
)
