package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Api
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.SocViewModel
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.StatusSafe
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ApiMiddlewareScreen(
    viewModel: SocViewModel,
    modifier: Modifier = Modifier
) {
    val selectedEndpoint by viewModel.apiSelectedEndpoint.collectAsStateWithLifecycle()
    val consoleInput by viewModel.apiConsoleInput.collectAsStateWithLifecycle()
    val consoleResponse by viewModel.apiConsoleResponse.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    var activeSubTab by remember { mutableIntStateOf(0) } // 0 = API Console, 1 = Middleware Guides

    val endpoints = listOf(
        "/api/analyze",
        "/api/batch-analyze",
        "/api/incidents",
        "/api/stats",
        "/api/rules",
        "/api/model-info",
        "/api/retrain"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("api_middleware_screen")
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Api, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "REST API & Middleware Integration",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
        Text(
            text = "Standalone Flask backend (sqli_detector/api.py) & web framework middleware",
            fontSize = 12.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Sub-tabs: API Console vs Middleware Guides
        TabRow(
            selectedTabIndex = activeSubTab,
            containerColor = CyberCardBg,
            contentColor = CyberCyan,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[activeSubTab]),
                    color = CyberCyan
                )
            },
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, CyberCardBorder, RoundedCornerShape(8.dp))
        ) {
            Tab(
                selected = activeSubTab == 0,
                onClick = { activeSubTab = 0 },
                text = { Text("Interactive API Console", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = activeSubTab == 1,
                onClick = { activeSubTab = 1 },
                text = { Text("Middleware Code Samples", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (activeSubTab == 0) {
            // Interactive API Test Console
            Text(
                text = "SELECT REST ENDPOINT",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = CyberCyan,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                endpoints.forEach { ep ->
                    val isSelected = selectedEndpoint == ep
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectApiEndpoint(ep) },
                        label = {
                            Text(
                                text = ep,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyberCyan.copy(alpha = 0.2f),
                            selectedLabelColor = CyberCyan,
                            containerColor = CyberCardBg,
                            labelColor = TextSecondary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Request Body Input
            Text("Request Payload (JSON)", fontSize = 11.sp, color = TextMuted)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = consoleInput,
                onValueChange = { viewModel.setApiConsoleInput(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF05080E),
                    unfocusedContainerColor = Color(0xFF05080E),
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedIndicatorColor = CyberCyan,
                    unfocusedIndicatorColor = CyberCardBorder
                ),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { viewModel.executeApiSimulation() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color(0xFF00363D))
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Simulate POST $selectedEndpoint", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Response Box
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("API Response Output", fontSize = 11.sp, color = TextMuted)
                OutlinedButton(
                    onClick = {
                        clipboard.setText(AnnotatedString(consoleResponse))
                        Toast.makeText(context, "API response copied", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.height(30.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(CyberCardBorder)
                    )
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy", fontSize = 10.sp)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF05080E))
                    .border(1.dp, CyberCardBorder, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = consoleResponse.ifBlank { "Click Simulate POST to inspect the live JSON response." },
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = if (consoleResponse.isNotBlank()) StatusSafe else TextMuted
                )
            }
        } else {
            // Middleware Code Samples
            val flaskCode = """
            # Flask Middleware (sqli_middleware.py)
            from flask import request, abort, jsonify
            import requests

            SQLI_DETECTOR_URL = "http://localhost:5000/api/analyze"

            def sqli_protection_middleware(app):
                @app.before_request
                def inspect_request():
                    payloads = []
                    for k, v in request.args.items():
                        payloads.append(v)
                    if request.is_json and request.json:
                        payloads.extend(str(x) for x in request.json.values())
                    
                    for text in payloads:
                        res = requests.post(SQLI_DETECTOR_URL, json={"payload": text, "client_ip": request.remote_addr}).json()
                        if res.get("classification") == "CRITICAL":
                            return jsonify({"error": "Request blocked by SQLi Shield", "score": res["composite_score"]}), 403
            """.trimIndent()

            val fastApiCode = """
            # FastAPI Middleware (main.py)
            from fastapi import FastAPI, Request, HTTPException
            from starlette.middleware.base import BaseHTTPMiddleware
            import httpx

            class SQLiDefenseMiddleware(BaseHTTPMiddleware):
                async def dispatch(self, request: Request, call_next):
                    query_str = str(request.query_params)
                    if query_str:
                        async with httpx.AsyncClient() as client:
                            resp = await client.post("http://localhost:5000/api/analyze", json={"payload": query_str})
                            data = resp.json()
                            if data.get("classification") == "CRITICAL":
                                raise HTTPException(status_code=403, detail="SQL Injection detected")
                    return await call_next(request)

            app = FastAPI()
            app.add_middleware(SQLiDefenseMiddleware)
            """.trimIndent()

            val expressCode = """
            // Express.js Middleware (sqliMiddleware.js)
            const axios = require('axios');

            const sqliShield = async (req, res, next) => {
              const checkValues = [...Object.values(req.query || {}), ...Object.values(req.body || {})];
              for (const val of checkValues) {
                if (typeof val === 'string') {
                  const resp = await axios.post('http://localhost:5000/api/analyze', {
                    payload: val,
                    client_ip: req.ip
                  });
                  if (resp.data.classification === 'CRITICAL') {
                    return res.status(403).json({ error: 'Blocked by SQLi Shield', alert_id: resp.data.alert_id });
                  }
                }
              }
              next();
            };

            app.use(sqliShield);
            """.trimIndent()

            CodeSampleCard(title = "Flask Python Middleware", code = flaskCode, clipboard = clipboard, context = context)
            Spacer(modifier = Modifier.height(12.dp))
            CodeSampleCard(title = "FastAPI Asynchronous Middleware", code = fastApiCode, clipboard = clipboard, context = context)
            Spacer(modifier = Modifier.height(12.dp))
            CodeSampleCard(title = "Express.js Node Middleware", code = expressCode, clipboard = clipboard, context = context)
        }
    }
}

@Composable
fun CodeSampleCard(
    title: String,
    code: String,
    clipboard: androidx.compose.ui.platform.ClipboardManager,
    context: android.content.Context
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(CyberCardBg)
            .border(1.dp, CyberCardBorder, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Code, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }

            OutlinedButton(
                onClick = {
                    clipboard.setText(AnnotatedString(code))
                    Toast.makeText(context, "Code copied to clipboard", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.height(28.dp),
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(CyberCardBorder)
                )
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Copy", fontSize = 10.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF05080E))
                .padding(10.dp)
        ) {
            Text(
                text = code,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFFE2E8F0),
                lineHeight = 14.sp
            )
        }
    }
}
