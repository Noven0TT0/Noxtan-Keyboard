package com.noxtan.noxboard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noxtan.noxboard.NoxBoardPrefs
import kotlinx.coroutines.launch

val emojiCategories = listOf(
    "Smileys & People" to listOf(
        "😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣", "🥲", "🥹", "☺️", "😊", "😇", "🙂", "🙃", "😉", "😌", "😍", "🥰", "😘", "😗", "😙", "😚", "😋", "😛", "😝", "😜", "🤪", "🤨", "🧐", "🤓", "😎", "🥸", "🤩", "🥳", "😏", "😒", "😞", "😔", "😟", "😕", "🙁", "☹️", "😣", "😖", "😫", "😩", "🥺", "😢", "😭", "😤", "🫩", "😠", "😡", "🤬", "🤯", "😳", "🥵", "🥶", "😱", "😨", "😰", "😥", "😓", "🤗", "🤔", "🤭", "🤫", "🤥", "😶", "😐", "😑", "😬", "🙄", "😯", "😦", "😧", "🥱", "😴", "🤤", "🫠", "🙂‍↕️", "🙂‍↔️", "😶‍🌫️", "🫥", "🫡", "🫢", "🫣", "😮‍💨", "🫤", "😵‍💫", "🫨", "😪", "😵", "🤐", "🥴", "🤢", "🤮", "🤧", "😷", "🤒", "🤕", "🫪", "🤑", "🤠", "😈", "👿", "👹", "👺", "🤡",
        "💩", "👻", "💀", "☠️", "👽", "👾", "🤖", "🎃", "😺", "😸", "😹", "😻", "😼", "😽", "🙀", "😿", "😾",
        "👋", "🤚", "🖐", "✋", "🖖", "👌", "🤌", "🤏", "✌️", "🤞", "🤟", "🤘", "🤙", "👈", "👉", "👆", "🖕", "👇", "☝️", "👍", "👎", "✊", "👊", "🤛", "🤜", "👏", "🙌", "👐", "🤲", "🤝", "🙏", "✍️", "💅", "🤳", "💪", "🦾", "🦿", "🦵", "🦶", "👂", "🦻", "👃", "🫀", "🫁", "🧠", "🦷", "🦴", "👀", "👁", "👅", "👄", "💋", "🩸", "🫆",
        "👶", "👼", "👧", "🧒", "👦", "👩", "🧑", "👨", "👩‍🦱", "🧑‍🦱", "👨‍🦱", "👩‍🦰", "🧑‍🦰", "👨‍🦰", "👱‍♀️", "👱", "👱‍♂️", "👩‍🦳", "🧑‍🦳", "👨‍🦳", "👩‍🦲", "🧑‍🦲", "👨‍🦲", "🧔‍♀️", "🧔", "🧔‍♂️", "👵", "🧓", "👴", "👲", "👳‍♀️", "👳", "👳‍♂️", "🧕",
        "🙍", "🙍‍♂️", "🙍‍♀️", "🙎", "🙎‍♂️", "🙎‍♀️", "🙅", "🙅‍♂️", "🙅‍♀️", "🙆", "🙆‍♂️", "🙆‍♀️", "💁", "💁‍♂️", "💁‍♀️", "🙋", "🙋‍♂️", "🙋‍♀️", "🧏", "🧏‍♂️", "🧏‍♀️", "🙇", "🙇‍♂️", "🙇‍♀️", "🤦", "🤦‍♂️", "🤦‍♀️", "🤷", "🤷‍♂️", "🤷‍♀️",
        "🧑‍⚕️", "👨‍⚕️", "👩‍⚕️", "🧑‍🎓", "👨‍🎓", "👩‍🎓", "🧑‍🏫", "👨‍🏫", "👩‍🏫", "🧑‍⚖️", "👨‍⚖️", "👩‍⚖️", "🧑‍🌾", "👨‍🌾", "👩‍🌾", "🧑‍🍳", "👨‍🍳", "👩‍🍳", "🧑‍🔧", "👨‍🔧", "👩‍🔧", "🧑‍🏭", "👨‍🏭", "👩‍🏭", "🧑‍💼", "👨‍💼", "👩‍💼", "🧑‍🔬", "👨‍🔬", "👩‍🔬", "🧑‍💻", "👨‍💻", "👩‍💻", "🧑‍🎤", "👨‍🎤", "👩‍🎤", "🧑‍🎨", "👨‍🎨", "👩‍🎨", "🧑‍🚀", "👨‍🚀", "👩‍🚀", "🧑‍🚒", "👨‍🚒", "👩‍🚒", "👮", "👮‍♂️", "👮‍♀️", "🕵️", "🕵️‍♂️", "🕵️‍♀️", "💂", "💂‍♂️", "💂‍♀️", "👷", "👷‍♂️", "👷‍♀️",
        "🫅", "🤴", "👸", "🤵", "🤵‍♂️", "🤵‍♀️", "👰", "👰‍♂️", "👰‍♀️", "🫄", "🤰", "🫃", "🤱", "🧑‍🍼", "👨‍🍼", "👩‍🍼",
        "🦸", "🦸‍♂️", "🦸‍♀️", "🦹", "🦹‍♂️", "🦹‍♀️", "🧙", "🧙‍♂️", "🧙‍♀️", "🧚", "🧚‍♂️", "🧚‍♀️", "🧛", "🧛‍♂️", "🧛‍♀️", "🧜", "🧜‍♂️", "🧜‍♀️", "🧝", "🧝‍♂️", "🧝‍♀️", "🧞", "🧞‍♂️", "🧞‍♀️", "🧟", "🧟‍♂️", "🧟‍♀️", "🧌", "🎅", "🤶", "🧑‍🎄", "🥷", "🕴️",
        "💆", "💆‍♂️", "💆‍♀️", "💇", "💇‍♂️", "💇‍♀️",
        "🚶", "🚶‍♂️", "🚶‍♀️", "🚶‍➡️", "🚶‍♂️‍➡️", "🚶‍♀️‍➡️", "🧍", "🧍‍♂️", "🧍‍♀️", "🧎", "🧎‍♂️", "🧎‍♀️", "🧎‍➡️", "🧎‍♂️‍➡️", "🧎‍♀️‍➡️", "🧑‍🦯", "👨‍🦯", "👩‍🦯", "🧑‍🦯‍➡️", "👨‍🦯‍➡️", "👩‍🦯‍➡️", "🏃", "🏃‍♂️", "🏃‍♀️", "🏃‍➡️", "🏃‍♂️‍➡️", "🏃‍♀️‍➡️", "💃", "🕺", "👯", "👯‍♂️", "👯‍♀️"
    ),
    "Animals & Nature" to listOf(
        "🐵", "🙈", "🙉", "🙊", "🐒", "🦍", "🦧", "🐶", "🐕", "🦮", "🐕‍🦺", "🐩", "🐺", "🦊", "🦝", "🐱", "🐈", "🐈‍⬛", "🦁", "🐯", "🐅", "🐆", "🐴", "🐎", "🦄", "🦓", "🦌", "🦬", "🐮", "🐂", "🐃", "🐄", "🐷", "🐖", "🐗", "🐽", "🐏", "🐑", "🐐", "🐪", "🐫", "🦙", "🦒", "🐘", "🦣", "🦏", "🦛", "🐭", "🐁", "🐀", "🐹", "🐰", "🐇", "🐿", "🦫", "🦔", "🦇", "🐻", "🐨", "🐼", "🦥", "🦦", "🦨", "🦘", "🦡", "🐾",
        "🦃", "🐔", "🐓", "🐣", "🐤", "🐥", "🐦", "🐦‍🔥", "🐧", "🕊", "🕊️", "🦅", "🦆", "🦢", "🦉", "🦤", "🪶", "🦩", "🦚", "🦜", "🪽", "🪺", "🪹",
        "🐸", "🐊", "🐢", "🦎", "🐍", "🐲", "🐉", "🦕", "🦖", "🐳", "🐋", "🐬", "🦭", "🐟", "🐠", "🐡", "🦈", "🦑", "🐙", "🐚", "🐌", "🦋", "🐛", "🐜", "🐝", "🪲", "🐞", "🦗", "🪳", "🕷", "🕸", "🦂", "🦟", "🪰", "🪱", "🦠",
        "💐", "🌸", "💮", "🏵", "🌹", "🥀", "🌺", "🌻", "🌼", "🌷", "🌱", "🪴", "🌲", "🌳", "🌴", "🌵", "🌾", "🌿", "☘️", "🍀", "🍁", "🍂", "🍃", "🍄", "🪨", "🪵",
        "☃️", "⛄", "🌬️", "🌚", "🌝", "🌞", "🌛", "🌜", "💫", "⭐", "🌟", "✨", "⚡", "💥", "🫯", "💨", "🔥"
    ),
    "Food & Drink" to listOf(
        "🍏", "🍎", "🍐", "🍊", "🍋", "🍌", "🍉", "🍇", "🍓", "🫐", "🍈", "🍒", "🍑", "🥭", "🍍", "🥥", "🥝", "🍅", "🍆", "🥑", "🥦", "🥬", "🥒", "🌶", "🫑", "🌽", "🥕", "🫒", "🧄", "🧅", "🥔", "🍠", "🥐", "🥯", "🍞", "🥖", "🥨", "🧀", "🥚", "🍳", "🧈", "🥞", "🧇", "🥓", "🥩", "🍗", "🍖", "🦴", "🌭", "🍔", "🍟", "🍕", "🫓", "🥪", "🥙", "🧆", "🌮", "🌯", "🫔", "🥗", "🥘", "🫕", "🥫", "🍝", "🍜", "🍲", "🍛", "🍣", "🍱", "🥟", "🦪", "🍤", "🍙", "🍚", "🍘", "🍥", "🥠", "🥮", "🍢", "🍡", "🍧", "🍨", "🍦", "🥧", "🧁", "🍰", "🎂", "🍮", "🍭", "🍬", "🍫", "🍿", "🍩", "🍪", "🌰", "🥜", "🍯", "🥛", "🍼", "🫖", "☕️", "🍵", "🧃", "🥤", "🧋", "🍶", "🍺", "🍻", "🥂", "🍷", "🥃", "🍸", "🍹", "🧉", "🍾", "🧊", "🥄", "🍴", "🍽", "🥣", "🥡", "🥢", "🧂"
    ),
    "Activities" to listOf(
        "⚽️", "🏀", "🏈", "⚾️", "🥎", "🎾", "🏐", "🏉", "🥏", "🎱", "🪀", "🏓", "🏸", "🏒", "🏑", "🥍", "🏏", "🪃", "🥅", "⛳️", "🪁", "🏹", "🎣", "🤿", "🥊", "🥋", "🎽", "🛹", "🛼", "🛷", "⛸", "🥌", "🎿", "⛷", "🏂", "🪂", "🏋️‍♀️", "🏋️", "🏋️‍♂️", "🤼‍♀️", "🤼", "🤼‍♂️", "🤸‍♀️", "🤸", "🤸‍♂️", "⛹️‍♀️", "⛹️", "⛹️‍♂️", "🤺", "🤾‍♀️", "🤾", "🤾‍♂️", "🏌️‍♀️", "🏌️", "🏌️‍♂️", "🏇", "🧘‍♀️", "🧘", "🧘‍♂️", "🌊", "🏄‍♀️", "🏄", "🏄‍♂️", "🏊‍♀️", "🏊", "🏊‍♂️", "🤽‍♀️", "🤽", "🤽‍♂️", "🚣‍♀️", "🚣", "🚣‍♂️", "🧗‍♀️", "🧗", "🧗‍♂️", "🚵‍♀️", "🚵", "🚵‍♂️", "🚴‍♀️", "🚴", "🚴‍♂️", "🏆", "🥇", "🥈", "🥉", "🏅", "🎖", "🏵", "🎗", "🎫", "🎟", "🎪", "🤹‍♀️", "🤹", "🤹‍♂️", "🎭", "🩰", "🎨", "🎬", "🎤", "🎧", "🎼", "🎹", "🥁", "🪘", "🎷", "🎺", "🪗", "🎸", "🪕", "🎻", "🎲", "♟", "🎯", "🎳", "🎮", "🎰", "🧩"
    ),
    "Travel & Places" to listOf(
        "🚗", "🚕", "🚙", "🚌", "🚎", "🏎", "🚓", "🚑", "🚒", "🚐", "🛻", "🚚", "🚛", "🚜", "🦯", "🦽", "🦼", "🛴", "🚲", "🛵", "🏍", "🛺", "🚨", "🚔", "🚍", "🚘", "🚖", "🚡", "🚠", "🚟", "🚃", "🚋", "🚞", "🚝", "🚄", "🚅", "🚈", "🚂", "🚆", "🚇", "🚊", "🚉", "🌤️", "🌥️", "☁️", "🌧️", "🌦️", "🌨️", "🌩️", "🌪️", "🌫️", "✈️", "🛫", "🛬", "🛩", "💺", "🛰", "🚀", "🛸", "🚁", "🛶", "⛵️", "🚤", "🛥", "🛳", "⛴", "🚢", "⚓️", "🪝", "⛽️", "🚧", "🚦", "🚥", "🚏", "🗺", "🗿", "🗽", "🗼", "🏰", "🏯", "🏟", "🎡", "🎢", "🎠", "⛲️", "⛱", "🏖", "🏖️", "🏝", "🏜", "🌋", "⛰", "🏔", "🏔️", "🗻", "🏕", "⛺️", "🛖", "🏠", "🏡", "🏘", "🏚", "🏗", "🏭", "🏢", "🏬", "🏣", "🏤", "🏥", "🏦", "🏨", "🏪", "🏫", "🏩", "💒", "🏛", "⛪️", "🕌", "🕍", "🛕", "🕋", "⛩", "🛤", "🛣", "🗾", "🎑", "🏞", "🌅", "🌄", "🌠", "🎇", "🎆", "🌇", "🌆", "🏙", "🌃", "🌌", "🌉", "🌁"
    ),
    "Objects" to listOf(
        "⌚️", "📱", "📲", "💻", "⌨️", "🖥", "🖨", "🖱", "🖲", "🕹", "🗜", "💽", "💾", "💿", "📀", "📼", "📷", "📸", "📹", "🎥", "📽", "🎞", "📞", "☎️", "📟", "📠", "📺", "📻", "🎙", "🎚", "🎛", "🧭", "⏱", "⏲", "⏰", "🕰", "⌛️", "⏳", "📡", "🔋", "🔌", "💡", "🔦", "🕯", "🪔", "🧯", "🛢", "💸", "💵", "💴", "💶", "💷", "🪙", "💰", "💳", "💎", "⚖️", "🪜", "🧰", "🪛", "🔧", "🔨", "⚒", "🛠", "⛏", "🪚", "🔩", "⚙️", "🪤", "🧱", "⛓", "🧲", "🔫", "💣", "🧨", "🪓", "🔪", "🗡", "⚔️", "🛡", "🚬", "⚰️", "🪦", "⚱️", "🏺", "🔮", "🪄", "🧿", "💈", "⚗️", "🔭", "🔬", "🕳", "🩹", "🩺", "💊", "💉", "🩸", "🧬", "🦠", "🧫", "🧪", "🌡", "🧹", "🪠", "🧺", "🧻", "🚽", "🚰", "🚿", "🛁", "🛀", "🧼", "🪥", "🧽", "🪣", "🧴", "🛎", "🔑", "🗝", "🚪", "🪑", "🛋", "🛏", "🛌", "🧸", "🪆", "🖼", "🪞", "🪟", "🛍", "🛒", "🎁", "🎈", "🎏", "🎀", "🪄", "🪅", "🎊", "🎉", "🎎", "🏮", "🎐", "🧧", "✉️", "📩", "📨", "📧", "💌", "📥", "📤", "📦", "🏷", "🪧", "📪", "📫", "📬", "📭", "📮", "📯", "📜", "📃", "📄", "📑", "🧾", "📊", "📈", "📉", "🗒", "🗓", "📆", "📅", "🗑", "📇", "🗃", "🗳", "🗄", "📋", "📁", "📂", "🗂", "🗞", "📰", "📓", "📔", "📒", "📕", "📗", "📘", "📙", "📚", "📖", "🔖", "🧷", "🔗", "📎", "🖇", "✂️", "📐", "📏", "🧮", "📌", "📍", "🎌", "🏳️‍🌈", "🏳️‍⚧️", "🏴‍☠️",
        "🧥", "👙", "🥾"
    ),
    "Symbols" to listOf(
        "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍", "🤎", "💔", "❣️", "💕", "💞", "💓", "💗", "💖", "💘", "💝", "💟",
        "🩵", "🩶", "🩷",
        "☮️", "✝️", "☪️", "🕉", "☸️", "✡️", "🔯", "🕎", "☯️", "☦️", "🛐", "⛎", "♈️", "♉️", "♊️", "♋️", "♌️", "♍️", "♎️", "♏️", "♐️", "♑️", "♒️", "♓️", "🆔", "⚛️", "⚕️", "☢️", "☣️", "📴", "📳", "🈶", "🈚️", "🈸", "🈺", "🈷️", "✴️", "🆚", "💮", "🉐", "㊙️", "㊗️", "🈴", "🈵", "🈹", "🈲", "🅰️", "🅱️", "🆎", "🆑", "🅾️", "🆘", "❌", "⭕️", "🛑", "⛔️", "📛", "🚫", "💯", "💢", "♨️", "🚷", "🚯", "🚳", "🚱", "🔞", "📵", "🚭", "❗️", "❕", "❓", "❔", "‼️", "⁉️", "🔅", "🔆", "〽️", "⚠️", "🚸", "🔱", "⚜️", "🔰", "♻️", "✅", "🈯️", "💹", "❇️", "✳️", "❎", "🌐", "💠", "Ⓜ️", "🌀", "💤", "🏧", "🚾", "♿️", "🅿️", "🛗", "🈳", "🈂️", "🛂", "🛃", "🛄", "🛅", "🚹", "🚺", "🚼", "⚧", "🚻", "🚮", "🎦", "📶", "🈁", "🔣", "ℹ️", "🔤", "🔡", "🔠", "🆖", "🆗", "🆙", "🆒", "🆕", "🆓", "0️⃣", "1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣", "6️⃣", "7️⃣", "8️⃣", "9️⃣", "🔟", "🔢", "#️⃣", "*️⃣", "⏏️", "▶️", "⏸", "⏯", "⏹", "⏺", "⏭", "⏮", "⏩", "⏪", "⏫", "⏬", "◀️", "🔼", "🔽", "➡️", "⬅️", "⬆️", "⬇️", "↗️", "↘️", "↙️", "↖️", "↕️", "↔️", "↪️", "↩️", "⤴️", "⤵️", "🔀", "🔁", "🔂", "🔄", "🔃", "🎵", "🎶", "➕", "➖", "➗", "✖️", "♾", "💲", "💱", "™️", "©️", "®️", "〰️", "➰", "➿", "🔚", "🔙", "🔛", "🔝", "🔜", "✔️", "☑️", "🔘", "🔴", "🟠", "🟡", "🟢", "🔵", "🟣", "⚫️", "⚪️", "🟤", "🔺", "🔻", "🔸", "🔹", "🔶", "🔷", "🔳", "🔲", "▪️", "▫️", "◾️", "◽️", "◼️", "◻️", "🟥", "🟧", "🟨", "🟩", "🟦", "🟪", "⬛️", "⬜️", "🟫", "🔈", "🔇", "🔉", "🔊", "🔔", "🔕", "📣", "📢", "👁‍🗨", "💬", "💭", "🗯", "♠️", "♣️", "♥️", "♦️", "🃏", "🎴", "🀄️", "🕐", "🕑", "🕒", "🕓", "🕔", "🕕", "🕖", "🕗", "🕘", "🕙", "🕚", "🕛", "🕜", "🕝", "🕞", "🕟", "🕠", "🕡", "🕢", "🕣", "🕤", "🕥", "🕦", "🕧",
        "🛜"
    ),
    "Flags" to listOf(
        "🏁", "🚩", "🎌", "🏴", "🏳️", "🏳️‍🌈", "🏳️‍⚧️", "🏴‍☠️", "🇦🇩", "🇦🇪", "🇦🇫", "🇦🇬", "🇦🇮", "🇦🇱", "🇦🇲", "🇦🇴", "🇦🇶", "🇦🇷", "🇦🇸", "🇦🇹", "🇦🇺", "🇦🇼", "🇦🇽", "🇦🇿", "🇧🇦", "🇧🇧", "🇧🇩", "🇧🇪", "🇧🇫", "🇧🇬", "🇧🇭", "🇧🇮", "🇧🇯", "🇧🇱", "🇧🇲", "🇧🇳", "🇧🇴", "🇧🇶", "🇧🇷", "🇧🇸", "🇧🇹", "🇧🇻", "🇧🇼", "🇧🇾", "🇧🇿", "🇨🇦", "🇨🇨", "🇨🇩", "🇨🇫", "🇨🇬", "🇨🇭", "🇨🇮", "🇨🇰", "🇨🇱", "🇨🇲", "🇨🇳", "🇨🇴", "🇨🇵", "🇨🇷", "🇨🇺", "🇨🇻", "🇨🇼", "🇨🇽", "🇨🇾", "🇨🇿", "🇩🇪", "🇩🇬", "🇩🇯", "🇩🇰", "🇩🇲", "🇩🇴", "🇩🇿", "🇪🇦", "🇪🇨", "🇪🇪", "🇪🇬", "🇪🇭", "🇪🇷", "🇪🇸", "🇪🇹", "🇪🇺", "🇫🇮", "🇫🇯", "🇫🇰", "🇫🇲", "🇫🇴", "🇫🇷", "🇬🇦", "🇬🇧", "🇬🇩", "🇬🇪", "🇬🇫", "🇬🇬", "🇬🇭", "🇬🇮", "🇬🇱", "🇬🇲", "🇬🇳", "🇬🇵", "🇬🇶", "🇬🇷", "🇬🇸", "🇬🇹", "🇬🇺", "🇬🇼", "🇬🇾", "🇭🇰", "🇭🇲", "🇭🇳", "🇭🇷", "🇭🇹", "🇭🇺", "🇮🇨", "🇮🇩", "🇮🇪", "🇮🇱", "🇮🇲", "🇮🇳", "🇮🇴", "🇮🇶", "🇮🇷", "🇮🇸", "🇮🇹", "🇯🇪", "🇯🇲", "🇯🇴", "🇯🇵", "🇰🇪", "🇰🇬", "🇰🇭", "🇰🇮", "🇰🇲", "🇰🇳", "🇰🇵", "🇰🇷", "🇰🇼", "🇰🇾", "🇰🇿", "🇱🇦", "🇱🇧", "🇱🇨", "🇱🇮", "🇱🇰", "🇱🇷", "🇱🇸", "🇱🇹", "🇱🇺", "🇱🇻", "🇱🇾", "🇲🇦", "🇲🇨", "🇲🇩", "🇲🇪", "🇲🇫", "🇲🇬", "🇲🇭", "🇲🇰", "🇲🇱", "🇲🇲", "🇲🇳", "🇲🇴", "🇲🇵", "🇲🇶", "🇲🇷", "🇲🇸", "🇲🇹", "🇲🇺", "🇲🇻", "🇲🇼", "🇲🇽", "🇲🇾", "🇲🇿", "🇳🇦", "🇳🇨", "🇳🇪", "🇳🇫", "🇳🇬", "🇳🇮", "🇳🇱", "🇳🇴", "🇳🇵", "🇳🇷", "🇳🇺", "🇳🇿", "🇴🇲", "🇵🇦", "🇵🇪", "🇵🇫", "🇵🇬", "🇵🇭", "🇵🇰", "🇵🇱", "🇵🇲", "🇵🇳", "🇵🇷", "🇵🇸", "🇵🇹", "🇵🇼", "🇵🇾", "🇶🇦", "🇷🇪", "🇷🇴", "🇷🇸", "🇷🇺", "🇷🇼", "🇸🇦", "🇸🇧", "🇸🇨", "🇸🇩", "🇸🇪", "🇸🇬", "🇸🇭", "🇸🇮", "🇸🇯", "🇸🇰", "🇸🇱", "🇸🇲", "🇸🇳", "🇸🇴", "🇸🇷", "🇸🇸", "🇸🇹", "🇸🇻", "🇸🇽", "🇸🇾", "🇸🇿", "🇹🇦", "🇹🇨", "🇹🇩", "🇹🇫", "🇹🇬", "🇹🇭", "🇹🇯", "🇹🇰", "🇹🇱", "🇹🇲", "🇹🇳", "🇹🇴", "🇹🇷", "🇹🇹", "🇹🇻", "🇹🇼", "🇹🇿", "🇺🇦", "🇺🇬", "🇺🇲", "🇺🇳", "🇺🇸", "🇺🇾", "🇺🇿", "🇻🇦", "🇻🇨", "🇻🇪", "🇻🇬", "🇻🇮", "🇻🇳", "🇻🇺", "🇼🇫", "🇼🇸", "🇽🇰", "🇾🇪", "🇾🇹", "🇿🇦", "🇿🇲", "🇿🇼", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", "🏴󠁧󠁢󠁳󠁣󠁴󠁿", "🏴󠁧󠁢󠁷󠁬󠁳󠁿"
    )
)

val kaomojiCategories = listOf(
    "Love & Cute" to listOf(
        "(\\_/)\n( •_•)\n/>❤️", "(✿◠‿◠)つ🌸", "( ˘ ³˘)♥", "(づ｡◕‿‿◕｡)づ",
        "(｡♥‿♥｡)", "ଘ(੭ˊᵕˋ)੭* ੈ✩‧₊˚", "(੭ˊᵕˋ)੭ 🌸", "( ˘ ³˘)❤️",
        "(\\_/)\n( •_•)\n/>🍰", "(｡•́‿•̀｡)⊃━☆ﾟ.*･｡ﾟ", "♡(> ਊ <)♡", "(*˘︶˘*).｡.:*♡",
        "(｡♥‿♥｡)", "(⁄ ⁄•⁄ω⁄•⁄ ⁄)", "(*♡∀♡)", "(´｡• ᵕ •｡`) ♡",
        "ლ(⌒▽⌒ლ)", "(˵¯͒〰¯͒˵)", "(๑´• .̫ •ू`๑)", "꒰ღ˘‿˘ற꒱❤⃛", "✿♥‿♥✿",
        "ヽ(♡‿♡)ノ", "ღゝ◡╹)ノ♡", "(⺣◡⺣)♡*",
    ),
    "Action & Weapons" to listOf(
        "▄︻̷̿┻̿═━一", "︻╦̵̵͇̿̿̿̿══╤─", "(ง'̀-'́)ง", "(╯°□°)╯︵ ┻━┻",
        "(ノಠ益ಠ)ノ彡┻━┻", "(•_•) ᢚ", "(ง •_•)ง", "(ง︡'-'︠)ง",
        "┬─┬ノ( º _ ºノ)", "(งᵒ̌皿ᵒ̌)ง⁼³₌₃", "凸(•‿•)凸", "( ಠ ʖ̯ ಠ)┌∩┐",
        "o==[::::::::>", "⚔️(•_•)ᢚ", "==|━━━━", "(ノ-_-)ノ ~┻━┻"
    ),
    "Classic & Shrug" to listOf(
        "¯\\_(ツ)_/¯", "( ͡° ͜ʖ ͡°)", "(•‿•)", "(>_<)",
        "(T_T)", "(¬_¬)", "╮(─▽─)╭", "¯\\(°_o)/¯",
        "(ʘ‿ʘ)", "(¬‿¬)", "ಠ_ಠ", "(งツ)ว",
        "(•ิ_•ิ)", "(;´༎ຶД༎ຶ`)", "(ಥ﹏ಥ)", "¯\\_( ͡° ͜ʖ ͡°)_/¯",
        "(눈‸눈)", "( •_•)", "༼ ༎ຶ ᆺ ༎ຶ༽",
        "⁀⊙෴☉⁀", "๑(◕‿◕)๑", "٩(＾◡＾)۶",
        "( ͡° ͜ʖ ͡° )", "(ᗒᗣᗕ)՞", "ಥ‿ಥ", "(☉_☉)",
        "( ؕؔʘ̥̥̥̥ ه ؔؕʘ̥̥̥̥ )?", "(｡-_-｡ )人( ｡-_-｡)", "( ؕؔʘ̥̥̥̥ ه ؔؕʘ̥̥̥̥ )?",
        "(☝ ՞ਊ ՞)☝", "(づ ՞ਊ ՞ )づ", "(｡-_-｡ )人( ｡-_-｡)",
        "(✿˘ω˘)˘ε˘˶ )", "❀◕ ‿ ◕❀", "(◕ ɔ ◕✿)",
        "ε=ε=(っ* ´□` )っ", "(●´⌓`●)", "(Θ︹Θ)ს",
        "⊂◉‿◉つ", "¯\\_༼ ಥ ‿ ಥ ༽_/¯", "乁༼☯‿☯✿༽ㄏ",
        "(｡•́︿•̀｡)", "(◕︿◕✿)", "(⌐▨_▨)",
        "⊂(▀¯▀⊂ )", "(⌐▀͡ ̯ʖ▀)", "☀(▀U ▀-͠)",
        "┏(‘▀_▀’)ノ♬♪", "(˵•́ ◡(̿▨-▨¬)̄", "┌(▀Ĺ̯▀)┐",
        "(ಥ﹏ಥ)", "( ◑‿◑)ɔ┏🍟-", "-🍔┑٩(^◡^ )", "🍲-(-‿- )"
    ),
    "Animals" to listOf(
        "(ᵔᴥᵔ)", "(=^･ω･^=)", "ʕ •ᴥ•ʔ", "(=^･ｪ･^=)",
        "(・(ｪ)・)", "(V•ᴥ•V)", "( ᐢ. ̫ .ᐢ )", "( ˵ •̀ ᴗ - ˵ )",
        "ᕦ(ò_óˇ)ᕤ", "૮ ˶ᵔ ᵕ ᵔ˶ ა", "(=`ω´=)", "ʕ ˵• ₒ •˵ ʔ",
        "(^・x・^)", "(,,◕ ⋏ ◕,,)", "/ᐠ｡ꞈ｡ᐟ\\", "ฅ^•ﻌ•^ฅ",
        "૮ ˶′ﻌ ‵˶ ა", "υ´• ﻌ •`υ", "૮ ˘ﻌ˘ ა", "໒( ̿･ ᴥ ̿･ )ʋ",
        "ʕっ•ᴥ•ʔっ", "▼・ᴥ・▼", "໒( ̿❍ ᴥ ̿❍)ʋ", "૮ ˆﻌˆ ა",
        "ʕっ•ᴥ•ʔっ", "ʕ'ᴥ'ʔっ", "ʕ•̫͡•ʕ*̫͡*ʕ•͓͡•ʔ-̫͡-ʕ•̫͡•ʔ*̫͡*ʔ-̫͡-ʔ"
    ),
    "Joy & Celebration" to listOf(
        "(ﾉ◕ヮ◕)ﾉ*:･ﾟ✧", "٩(◕‿◕｡)۶", "(★ω★)", "ᕕ( ᐛ )ᕗ",
        "(☞ﾟヮﾟ)☞", "＼(＾▽＾)／", "(b ᵔ▽ᵔ)b", "╰(▔∀▔)╯",
        "(✯◡✯)", "ヽ(>∀<☆)ノ", "(─‿‿─)", "(o˘◡˘o)",
        "(*¯ ³¯*)♡", "( ＾◡＾)っ✂", "ō≡o", "[____]___o≡o"
    )
)

sealed class EmojiGridItem {
    abstract val key: String
    data class Header(val title: String, override val key: String) : EmojiGridItem()
    data class Emoji(val emoji: String, override val key: String) : EmojiGridItem()
}

@Composable
fun EmojiPanel(
    bottomPaddingDp: Float,
    onClose: () -> Unit,
    onEmojiClick: (String) -> Unit,
    onBackspace: () -> Unit,
    onSpace: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { NoxBoardPrefs(context) }

    var currentMediaMode by rememberSaveable { mutableStateOf("EMOJI") }

    var displayRecents by remember {
        mutableStateOf(prefs.recentEmojis.split(",").filter { it.isNotEmpty() })
    }

    val bgColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val onSurface = MaterialTheme.colorScheme.onSurface

    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()

    val emojiCategoriesList = remember(displayRecents) {
        val list = mutableListOf<Pair<ImageVector, List<String>>>()
        list.add(Icons.Default.Schedule to displayRecents)
        list.add(Icons.Default.SentimentSatisfied to emojiCategories[0].second)
        list.add(Icons.Default.Pets to emojiCategories[1].second)
        list.add(Icons.Default.Restaurant to emojiCategories[2].second)
        list.add(Icons.Default.SportsSoccer to emojiCategories[3].second)
        list.add(Icons.Default.DirectionsCar to emojiCategories[4].second)
        list.add(Icons.Default.Lightbulb to emojiCategories[5].second)
        list.add(Icons.Default.EmojiSymbols to emojiCategories[6].second)
        list.add(Icons.Default.Flag to emojiCategories[7].second)
        list
    }

    val emojiCategoryNames = listOf("Recent", "Smileys & People", "Animals & Nature", "Food & Drink", "Activities", "Travel & Places", "Objects", "Symbols", "Flags")

    val emojiGridItems = remember(emojiCategoriesList) {
        val list = mutableListOf<EmojiGridItem>()
        emojiCategoriesList.forEachIndexed { index, category ->
            if (category.second.isNotEmpty()) {
                list.add(EmojiGridItem.Header(emojiCategoryNames[index], "tab_${index}_header_${emojiCategoryNames[index]}"))
                category.second.forEachIndexed { emojiIdx, emoji ->
                    val uniqueKey = if (index == 0) "tab_0_recent_$emoji" else "tab_${index}_emoji_${emojiIdx}_$emoji"
                    list.add(EmojiGridItem.Emoji(emoji, uniqueKey))
                }
            }
        }
        list
    }

    val emojiHeaderIndices = remember(emojiCategoriesList) {
        val indices = IntArray(emojiCategoriesList.size) { 0 }
        var currentIndex = 0
        emojiCategoriesList.forEachIndexed { index, category ->
            indices[index] = currentIndex
            if (category.second.isNotEmpty()) {
                currentIndex += 1 + category.second.size
            }
        }
        indices
    }

    val kaomojiIcons = listOf(
        Icons.Default.Favorite to kaomojiCategories[0].second,
        Icons.Default.FlashOn to kaomojiCategories[1].second,
        Icons.Default.SentimentSatisfiedAlt to kaomojiCategories[2].second,
        Icons.Default.Pets to kaomojiCategories[3].second,
        Icons.Default.Celebration to kaomojiCategories[4].second
    )

    val kaomojiCategoryNames = listOf("Love & Cute", "Action & Weapons", "Classic & Shrug", "Animals", "Joy & Celebration")

    val kaomojiGridItems = remember {
        val list = mutableListOf<EmojiGridItem>()
        kaomojiCategories.forEachIndexed { index, category ->
            list.add(EmojiGridItem.Header(kaomojiCategoryNames[index], "kao_tab_${index}_header"))
            category.second.forEachIndexed { kIdx, kao ->
                list.add(EmojiGridItem.Emoji(kao, "kao_tab_${index}_item_$kIdx"))
            }
        }
        list
    }

    val kaomojiHeaderIndices = remember {
        val indices = IntArray(kaomojiCategories.size) { 0 }
        var currentIndex = 0
        kaomojiCategories.forEachIndexed { index, category ->
            indices[index] = currentIndex
            currentIndex += 1 + category.second.size
        }
        indices
    }

    val activeTabIndex by remember(currentMediaMode) {
        derivedStateOf {
            val firstVisibleItem = gridState.layoutInfo.visibleItemsInfo.firstOrNull() ?: return@derivedStateOf if (currentMediaMode == "EMOJI") 1 else 0
            val key = firstVisibleItem.key as? String ?: return@derivedStateOf if (currentMediaMode == "EMOJI") 1 else 0
            if (currentMediaMode == "EMOJI" && key.startsWith("tab_")) {
                key.substringAfter("tab_").substringBefore("_").toIntOrNull() ?: 1
            } else if (currentMediaMode == "KAOMOJI" && key.startsWith("kao_tab_")) {
                key.substringAfter("kao_tab_").substringBefore("_").toIntOrNull() ?: 0
            } else {
                if (currentMediaMode == "EMOJI") 1 else 0
            }
        }
    }

    val handleEmojiClick = { emoji: String ->
        onEmojiClick(emoji)
        if (currentMediaMode == "EMOJI") {
            val dbRecents = prefs.recentEmojis.split(",").filter { it.isNotEmpty() }.toMutableList()
            dbRecents.remove(emoji)
            dbRecents.add(0, emoji)
            prefs.recentEmojis = dbRecents.take(36).joinToString(",")
            if (!displayRecents.contains(emoji)) {
                val newDisplay = displayRecents.toMutableList()
                newDisplay.add(0, emoji)
                displayRecents = newDisplay.take(36)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(bottom = bottomPaddingDp.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(bgColor),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentMediaMode == "EMOJI") {
                emojiCategoriesList.forEachIndexed { index, category ->
                    val isActive = activeTabIndex == index
                    val tintColor = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable {
                                coroutineScope.launch {
                                    if (category.second.isNotEmpty()) {
                                        gridState.scrollToItem(emojiHeaderIndices[index])
                                    } else if (index == 0) {
                                        gridState.scrollToItem(0)
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = category.first, contentDescription = null, tint = tintColor, modifier = Modifier.size(20.dp))
                        if (isActive) {
                            Box(modifier = Modifier.align(Alignment.BottomCenter).width(24.dp).height(3.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)))
                        }
                    }
                }
            } else {
                kaomojiIcons.forEachIndexed { index, category ->
                    val isActive = activeTabIndex == index
                    val tintColor = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable {
                                coroutineScope.launch {
                                    gridState.scrollToItem(kaomojiHeaderIndices[index])
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = category.first, contentDescription = null, tint = tintColor, modifier = Modifier.size(20.dp))
                        if (isActive) {
                            Box(modifier = Modifier.align(Alignment.BottomCenter).width(24.dp).height(3.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)))
                        }
                    }
                }
            }
        }

        val currentItems = if (currentMediaMode == "EMOJI") emojiGridItems else kaomojiGridItems

        LazyVerticalGrid(
            state = gridState,
            columns = if (currentMediaMode == "EMOJI") GridCells.Adaptive(minSize = 45.dp) else GridCells.Adaptive(minSize = 105.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 4.dp),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            items(
                count = currentItems.size,
                key = { index -> currentItems[index].key },
                span = { index ->
                    if (currentItems[index] is EmojiGridItem.Header) GridItemSpan(maxLineSpan) else GridItemSpan(1)
                }
            ) { index ->
                when (val item = currentItems[index]) {
                    is EmojiGridItem.Header -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 8.dp, end = 8.dp, top = 14.dp, bottom = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.title,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (currentMediaMode == "EMOJI" && item.title == "Recent") {
                                IconButton(
                                    onClick = {
                                        displayRecents = emptyList()
                                        prefs.recentEmojis = ""
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Clear Recent", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                    is EmojiGridItem.Emoji -> {
                        if (currentMediaMode == "EMOJI") {
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { handleEmojiClick(item.emoji) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = item.emoji, fontSize = 28.sp, textAlign = TextAlign.Center)
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .padding(3.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(surfaceColor)
                                    .clickable { handleEmojiClick(item.emoji) }
                                    .padding(horizontal = 4.dp, vertical = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = item.emoji,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = onSurface,
                                    textAlign = TextAlign.Center,
                                    maxLines = 3,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(bgColor)
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1.5f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(5.dp))
                    .background(surfaceColor)
                    .clickable { onClose() },
                contentAlignment = Alignment.Center
            ) {
                Text("Back", color = onSurface, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            Box(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(5.dp))
                    .background(if (currentMediaMode == "EMOJI") MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else surfaceColor)
                    .clickable {
                        currentMediaMode = "EMOJI"
                        coroutineScope.launch { gridState.scrollToItem(0) }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("😀", fontSize = 16.sp)
            }

            Box(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(5.dp))
                    .background(if (currentMediaMode == "KAOMOJI") MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else surfaceColor)
                    .clickable {
                        currentMediaMode = "KAOMOJI"
                        coroutineScope.launch { gridState.scrollToItem(0) }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(":-)", color = if (currentMediaMode == "KAOMOJI") MaterialTheme.colorScheme.primary else onSurface, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Box(
                modifier = Modifier
                    .weight(3.8f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(5.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable { onSpace() },
                contentAlignment = Alignment.Center
            ) {
                Text("Space", color = onSurface, fontSize = 14.sp)
            }

            var isDeleting by remember { mutableStateOf(false) }

            LaunchedEffect(isDeleting) {
                if (isDeleting) {
                    onBackspace()
                    kotlinx.coroutines.delay(400)
                    while (isDeleting) {
                        onBackspace()
                        kotlinx.coroutines.delay(50)
                    }
                }
            }

            Box(
                modifier = Modifier
                    .weight(1.5f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(5.dp))
                    .background(surfaceColor)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isDeleting = true
                                tryAwaitRelease()
                                isDeleting = false
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Backspace",
                    tint = onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}