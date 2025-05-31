@echo off
REM Deploy port-forward proxy
kubectl apply -f ./port-forward-proxy.yaml

REM Wait for proxy pod to start
echo Waiting for port-forward proxy to start...
timeout /t 10 > nul
kubectl wait --for=condition=ready pod -l app=port-forward-proxy --timeout=60s

if %errorlevel% neq 0 (
    echo Error: Proxy pod failed to start
    exit /b 1
)

REM Start port-forwarding
echo Starting persistent port-forward (localhost:8080 -> port-forward-proxy:8080)
echo Press Ctrl+C to stop port-forwarding
kubectl port-forward service/port-forward-proxy 8080:8080

REM Cleanup
echo Cleaning up proxy resources...
kubectl delete -f port-forward-proxy.yaml