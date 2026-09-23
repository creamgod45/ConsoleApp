package cg.creamgod.consoleapp.designsystem.components

import androidx.compose.material3.HorizontalDivider as MaterialHorizontalDivider
import androidx.compose.material3.VerticalDivider as MaterialVerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun HorizontalDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp,
    color: Color = Color.Unspecified,
) {
    if (color == Color.Unspecified) {
        MaterialHorizontalDivider(modifier = modifier, thickness = thickness)
    } else {
        MaterialHorizontalDivider(modifier = modifier, thickness = thickness, color = color)
    }
}

@Composable
fun VerticalDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp,
    color: Color = Color.Unspecified,
) {
    if (color == Color.Unspecified) {
        MaterialVerticalDivider(modifier = modifier, thickness = thickness)
    } else {
        MaterialVerticalDivider(modifier = modifier, thickness = thickness, color = color)
    }
}
