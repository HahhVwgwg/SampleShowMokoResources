package com.dna.payments.kmm.data.repository

import com.dna.payments.kmm.data.model.request.SendInstructionRequest
import com.dna.payments.kmm.domain.network.Response
import com.dna.payments.kmm.domain.repository.ResetPasswordRepository
import com.dna.payments.kmm.domain.repository.SendOtpInstructionsUseCase

class SendOtpInstructionsUseCaseImpl(private val resetPasswordRepository: ResetPasswordRepository) :
    SendOtpInstructionsUseCase {

    override suspend fun sendInstructions(email: String): Response<Unit> {
        return resetPasswordRepository.sendInstructions(SendInstructionRequest(email))
    }
}