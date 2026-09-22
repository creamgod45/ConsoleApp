package cg.creamgod.consoleapp.designsystem.content

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

@Composable
fun H1(text: String, modifier: Modifier = Modifier, color: Color = Color.Unspecified) =
    SemanticText(text, MaterialTheme.typography.displaySmall, modifier, color)

@Composable
fun H2(text: String, modifier: Modifier = Modifier, color: Color = Color.Unspecified) =
    SemanticText(text, MaterialTheme.typography.headlineLarge, modifier, color)

@Composable
fun H3(text: String, modifier: Modifier = Modifier, color: Color = Color.Unspecified) =
    SemanticText(text, MaterialTheme.typography.headlineMedium, modifier, color)

@Composable
fun H4(text: String, modifier: Modifier = Modifier, color: Color = Color.Unspecified) =
    SemanticText(text, MaterialTheme.typography.headlineSmall, modifier, color)

@Composable
fun H5(text: String, modifier: Modifier = Modifier, color: Color = Color.Unspecified) =
    SemanticText(text, MaterialTheme.typography.titleLarge, modifier, color)

@Composable
fun H6(text: String, modifier: Modifier = Modifier, color: Color = Color.Unspecified) =
    SemanticText(text, MaterialTheme.typography.titleMedium, modifier, color)

@Composable
fun Paragraph(text: String, modifier: Modifier = Modifier, color: Color = Color.Unspecified) =
    SemanticText(text, MaterialTheme.typography.bodyLarge, modifier, color)

@Composable
fun Lead(text: String, modifier: Modifier = Modifier, color: Color = Color.Unspecified) =
    SemanticText(text, MaterialTheme.typography.titleMedium, modifier, color)

@Composable
fun Caption(text: String, modifier: Modifier = Modifier, color: Color = Color.Unspecified) =
    SemanticText(text, MaterialTheme.typography.bodySmall, modifier, color)

@Composable
fun SmallText(text: String, modifier: Modifier = Modifier, color: Color = Color.Unspecified) =
    SemanticText(text, MaterialTheme.typography.labelMedium, modifier, color)

@Composable
private fun SemanticText(text: String, style: TextStyle, modifier: Modifier, color: Color) {
    Text(text = text, modifier = modifier, color = color, style = style)
}
