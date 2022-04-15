package models

data class EdgesByVisibility(
    val visibleEdges: List<Edge>,
    val invisibleEdges: List<Edge>,
)