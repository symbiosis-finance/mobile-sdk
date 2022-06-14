package com.symbiosis.sdk.swap.unified

import com.symbiosis.sdk.swap.crosschain.CrossChain

fun UnifiedSwapRepository(allCrossChains: List<CrossChain>): UnifiedSwapRepository {
    return UnifiedSwapRepository(DefaultUnifiedSwapRepositoryAdapter(allCrossChains))
}
