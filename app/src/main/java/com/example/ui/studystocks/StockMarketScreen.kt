package com.example.ui.studystocks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.TimeBasedThemeManager
import com.example.data.local.entities.Stock
import com.example.data.local.entities.StockPortfolio
import com.example.data.repository.StudyRepository
import com.example.ui.common.StudyIcons
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockMarketScreen(
    repository: StudyRepository,
    themeManager: TimeBasedThemeManager,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val totalFame by repository.totalFame.collectAsState(initial = 100)
    val stocks by repository.allStocks.collectAsState(initial = emptyList())
    val portfolio by repository.portfolio.collectAsState(initial = emptyList())

    var selectedStockToTrade by remember { mutableStateOf<Stock?>(null) }
    var tradeMode by remember { mutableStateOf("BUY") } // "BUY" or "SELL"
    var tradeQuantity by remember { mutableIntStateOf(5) }
    var showTradeDialog by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    // Calculate total portfolio value in Fame (1 dollar = 10 Fame)
    val totalPortfolioFameValue = remember(portfolio, stocks) {
        portfolio.sumOf { pos ->
            val st = stocks.firstOrNull { it.id == pos.stockId }
            val currentPr = st?.currentPrice ?: pos.averageBuyPrice
            (pos.sharesOwned * currentPr * 10).toInt()
        }
    }

    val isNightMode = themeManager.isDarkThemeActive()
    val backgroundBrush = remember(isNightMode) {
        if (isNightMode) Brush.verticalGradient(listOf(Color(0xFF4A2C2C), Color(0xFF241515)))
        else Brush.verticalGradient(listOf(Color(0xFFD9534F), Color(0xFFC94440)))
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Study Stocks Market",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = StudyIcons.Back,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
            // Portfolio Summary Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryNightCard),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "YOUR HOLDINGS",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = FameGold,
                                letterSpacing = 1.sp
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = StudyIcons.FameStar,
                                    contentDescription = "Fame",
                                    tint = FameGold,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "$totalFame Available",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    text = "$totalPortfolioFameValue Fame",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 24.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "Portfolio Value (${portfolio.sumOf { it.sharesOwned }} shares)",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SuccessGreen.copy(alpha = 0.25f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = StudyIcons.StudyStocks,
                                    contentDescription = "Trending",
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "+16.8% All-Time",
                                    color = SuccessGreen,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Market Explainer
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = StudyIcons.StudyStocks,
                        contentDescription = "Info",
                        tint = FameGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Stock prices rise when students log more study hours in that subject. Invest your Fame wisely!",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            item {
                Text(
                    text = "Live Subject Stocks",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            // Stock Items
            items(stocks, key = { it.id }) { stock ->
                val userHolding = portfolio.firstOrNull { it.stockId == stock.id }
                StockCard(
                    stock = stock,
                    userHolding = userHolding,
                    onBuyClick = {
                        selectedStockToTrade = stock
                        tradeMode = "BUY"
                        tradeQuantity = 5
                        showTradeDialog = true
                    },
                    onSellClick = {
                        selectedStockToTrade = stock
                        tradeMode = "SELL"
                        tradeQuantity = userHolding?.sharesOwned ?: 1
                        showTradeDialog = true
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
    }

    // Buy / Sell Trade Dialog
    if (showTradeDialog && selectedStockToTrade != null) {
        val stock = selectedStockToTrade!!
        val userHolding = portfolio.firstOrNull { it.stockId == stock.id }
        val costInFame = (stock.currentPrice * tradeQuantity * 10).toInt()

        AlertDialog(
            onDismissRequest = { showTradeDialog = false },
            title = {
                Text(
                    text = if (tradeMode == "BUY") "Buy ${stock.symbol} Shares" else "Sell ${stock.symbol} Shares",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "${stock.subjectName} currently trades at \$${String.format("%.2f", stock.currentPrice)} (${(stock.currentPrice * 10).toInt()} Fame/share)",
                        fontSize = 13.sp,
                        color = OnSurfaceDark
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Quantity:", fontWeight = FontWeight.SemiBold)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { if (tradeQuantity > 1) tradeQuantity -= 1 },
                                shape = CircleShape,
                                modifier = Modifier.size(34.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) { Text("-", fontWeight = FontWeight.Black) }

                            Text(
                                text = "$tradeQuantity",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )

                            Button(
                                onClick = { tradeQuantity += 1 },
                                shape = CircleShape,
                                modifier = Modifier.size(34.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) { Text("+", fontWeight = FontWeight.Black) }
                        }
                    }

                    Divider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (tradeMode == "BUY") "Total Cost:" else "Total Proceeds:",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$costInFame Fame",
                            fontWeight = FontWeight.Black,
                            color = if (tradeMode == "BUY") WarningRed else SuccessGreen
                        )
                    }

                    if (tradeMode == "BUY" && costInFame > totalFame) {
                        Text(
                            text = "Insufficient Fame! (You have $totalFame Fame)",
                            color = WarningRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            if (tradeMode == "BUY") {
                                val success = repository.buyStock(stock, tradeQuantity)
                                if (success) snackbarMessage = "Purchased $tradeQuantity shares of ${stock.symbol}!"
                                else snackbarMessage = "Could not purchase: Insufficient Fame."
                            } else {
                                val success = repository.sellStock(stock, tradeQuantity)
                                if (success) snackbarMessage = "Sold $tradeQuantity shares of ${stock.symbol}!"
                            }
                            showTradeDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (tradeMode == "BUY") AccentTeal else SuccessGreen
                    ),
                    enabled = (tradeMode == "SELL" && (userHolding?.sharesOwned ?: 0) >= tradeQuantity) ||
                            (tradeMode == "BUY" && costInFame <= totalFame)
                ) {
                    Text(if (tradeMode == "BUY") "Confirm Buy" else "Confirm Sell")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTradeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun StockCard(
    stock: Stock,
    userHolding: StockPortfolio?,
    onBuyClick: () -> Unit,
    onSellClick: () -> Unit
) {
    val isPositive = stock.weeklyPercentChange >= 0

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCream),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = stock.symbol,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = OnSurfaceDark
                        )
                        Text(
                            text = "• ${stock.subjectName}",
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = OnSurfaceMuted
                        )
                    }
                    Text(
                        text = "Weekly Study Volume: ${stock.studyVolumeThisWeek} hrs logged",
                        fontSize = 11.sp,
                        color = OnSurfaceMuted
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "\$${String.format("%.2f", stock.currentPrice)}",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = OnSurfaceDark
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = if (isPositive) StudyIcons.ArrowUp else StudyIcons.ArrowDown,
                            contentDescription = null,
                            tint = if (isPositive) SuccessGreen else WarningRed,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "${if (isPositive) "+" else ""}${String.format("%.1f", stock.weeklyPercentChange)}%",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (isPositive) SuccessGreen else WarningRed
                        )
                    }
                }
            }

            // User Holding Status & Buy/Sell Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (userHolding != null && userHolding.sharesOwned > 0)
                        "You own: ${userHolding.sharesOwned} shares"
                    else "No position owned",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryCoralDark
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (userHolding != null && userHolding.sharesOwned > 0) {
                        OutlinedButton(
                            onClick = onSellClick,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Sell", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    Button(
                        onClick = onBuyClick,
                        colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("Buy", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
