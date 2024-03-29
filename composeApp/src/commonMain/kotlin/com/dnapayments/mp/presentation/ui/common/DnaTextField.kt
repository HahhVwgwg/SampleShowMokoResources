package com.dnapayments.mp.presentation.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.dnapayments.mp.MR
import com.dnapayments.mp.presentation.model.TextFieldUiState
import com.dnapayments.mp.presentation.theme.Dimens
import com.dnapayments.mp.presentation.theme.DnaTextStyle
import com.dnapayments.mp.presentation.theme.Paddings
import com.dnapayments.mp.presentation.theme.black
import com.dnapayments.mp.presentation.theme.greenButtonNotFilled
import com.dnapayments.mp.presentation.theme.greyColor
import com.dnapayments.mp.presentation.theme.outlineGreenColor
import com.dnapayments.mp.presentation.theme.poppinsFontFamily
import com.dnapayments.mp.presentation.theme.red
import com.dnapayments.mp.presentation.theme.white
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun DNAEmailTextField(
    textState: TextFieldUiState,
    placeholder: String = stringResource(MR.strings.email)
) {
    DnaTextField(
        textState = textState,
        placeholder = placeholder,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            autoCorrect = false
        ),
    )
}

@Composable
fun DNAVerificationCodeTextField(textState: TextFieldUiState) {
    DnaTextField(
        textState = textState,
        placeholder = stringResource(MR.strings.verification_code_placeholder),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            autoCorrect = false
        ),
        maxLength = 6
    )
}

@Composable
fun DNAPasswordTextField(
    textState: TextFieldUiState,
) {
    var passwordVisibility by remember { mutableStateOf(false) }
    val icon = if (passwordVisibility)
        painterResource(MR.images.ic_visibility)
    else
        painterResource(MR.images.ic_invisibility)

    DnaTextField(
        textState = textState,
        placeholder = stringResource(MR.strings.password),
        trailingIcon = {
            Icon(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp).clickable(
                    indication = null,
                    interactionSource = MutableInteractionSource(),
                    onClick = { passwordVisibility = !passwordVisibility }
                )
            )
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            autoCorrect = false
        ),
        visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
    )
}

@Composable
fun DnaTextField(
    textState: TextFieldUiState,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    trailingIcon: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    maxLength: Int = 0,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        modifier = modifier,
        enabled = enabled,
        value = textState.input.value,
        onValueChange = {
            if (maxLength == 0 || it.length <= maxLength) {
                textState.input.value = it
                textState.onFieldChanged()
            }
        },
        shape = RoundedCornerShape(Paddings.standard),
        textStyle = DnaTextStyle.Normal16.copy(
            fontFamily = poppinsFontFamily()
        ),
        keyboardOptions = keyboardOptions,
        supportingText = {
            if (!textState.validationResult.value.successful) {
                DNAText(
                    textState.validationResult.value.errorMessage?.getText() ?: "",
                    style = DnaTextStyle.MediumRed16,
                    modifier = Modifier.padding(start = 0.dp)
                )
            }
        },
        isError = !textState.validationResult.value.successful,
        placeholder = {
            DNAText(
                placeholder,
                style = DnaTextStyle.WithAlpha16
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            cursorColor = greyColor,
            disabledLabelColor = greyColor,
            disabledBorderColor = greyColor,
            unfocusedBorderColor = greyColor,
            focusedBorderColor = outlineGreenColor,
            focusedContainerColor = white,
            errorTextColor = red
        ),
        singleLine = singleLine,
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        leadingIcon = leadingIcon,
        readOnly = readOnly
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = OutlinedTextFieldDefaults.shape,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors()
) {
    val mergedTextStyle = textStyle.merge(TextStyle(color = black))

    BasicTextField(
        value = value,
        modifier = modifier.fillMaxWidth().defaultMinSize(minHeight = Dimens.textFieldHeight),
        onValueChange = onValueChange,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = mergedTextStyle,
        cursorBrush = SolidColor(greenButtonNotFilled),
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        interactionSource = interactionSource,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        decorationBox = @Composable { innerTextField ->
            OutlinedTextFieldDefaults.DecorationBox(
                value = value,
                visualTransformation = visualTransformation,
                innerTextField = innerTextField,
                placeholder = placeholder,
                label = label,
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
                prefix = prefix,
                suffix = suffix,
                supportingText = supportingText,
                singleLine = singleLine,
                enabled = enabled,
                isError = isError,
                interactionSource = interactionSource,
                colors = colors,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                container = {
                    OutlinedTextFieldDefaults.ContainerBox(
                        enabled,
                        isError,
                        interactionSource,
                        colors,
                        shape
                    )
                }
            )
        }
    )
}