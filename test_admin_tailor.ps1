# Test Admin and Tailor Modules End-to-End
$ErrorActionPreference = "Stop"

function Test-Endpoint {
    param($Title, $ScriptBlock)
    Write-Host "`n=== [TEST] $Title ===" -ForegroundColor Cyan
    try {
        & $ScriptBlock
        Write-Host ">>> PASS: $Title" -ForegroundColor Green
    } catch {
        Write-Host ">>> FAIL: $Title" -ForegroundColor Red
        Write-Host "Error: $_" -ForegroundColor DarkRed
        throw $_
    }
}

$baseUrl = "http://localhost:8080/api"

# 1. Admin Login
$adminUser = $null
Test-Endpoint "Admin Authentication" {
    $body = @{
        email = "admin@customclothing.com"
        password = "admin123"
    } | ConvertTo-Json
    $res = Invoke-RestMethod -Uri "$baseUrl/users/login" -Method Post -ContentType "application/json" -Body $body
    if (-not $res.success -or $res.data.role -ne "ADMIN") {
        throw "Failed to login as admin: $($res.message)"
    }
    $script:adminUser = $res.data
    Write-Host "Logged in as Admin: $($adminUser.fullName) (ID: $($adminUser.id), Role: $($adminUser.role))"
}

# 2. Admin Dashboard Data
Test-Endpoint "Admin Dashboard Queries" {
    $orders = Invoke-RestMethod -Uri "$baseUrl/orders"
    $products = Invoke-RestMethod -Uri "$baseUrl/products?all=true"
    $customers = Invoke-RestMethod -Uri "$baseUrl/users/customers"
    $tailors = Invoke-RestMethod -Uri "$baseUrl/users/tailors"

    Write-Host "Total Orders: $($orders.data.Count)"
    Write-Host "Total Products: $($products.data.Count)"
    Write-Host "Total Customers: $($customers.data.Count)"
    Write-Host "Total Tailors: $($tailors.data.Count)"

    if ($tailors.data.Count -lt 2) {
        throw "Expected at least 2 tailors"
    }
}

# 3. Admin Product Management (Create, Update, Toggle)
$createdProductId = $null
Test-Endpoint "Admin Create, Update, and Toggle Product" {
    $newProd = @{
        name = "Royal Cashmere Overcoat"
        description = "Pure double-faced Mongolian cashmere with horn buttons."
        basePrice = 32999.00
        categoryId = 1
        availableSizes = "38R, 40R, 42R, 44R, Custom Made-to-Measure"
        availableColors = "Camel, Charcoal, Midnight Navy"
        availableFabrics = "100% Mongolian Cashmere, Wool-Cashmere Blend"
        imageUrl = "https://images.unsplash.com/photo-1593030761757-71fae45fa0e7?auto=format&fit=crop&w=800&q=80"
        active = $true
    } | ConvertTo-Json

    $createRes = Invoke-RestMethod -Uri "$baseUrl/products" -Method Post -ContentType "application/json" -Body $newProd
    if (-not $createRes.success -or -not $createRes.data.id) {
        throw "Failed to create product"
    }
    $script:createdProductId = $createRes.data.id
    Write-Host "Created Product ID: $createdProductId, BasePrice: INR $($createRes.data.basePrice)"

    # Update product
    $updateProd = @{
        name = "Royal Cashmere Overcoat (Updated)"
        description = "Updated luxury Mongolian cashmere description."
        basePrice = 35999.00
        categoryId = 1
        availableSizes = "38R, 40R, 42R, 44R, Custom Made-to-Measure"
        availableColors = "Camel, Charcoal, Midnight Navy"
        availableFabrics = "100% Mongolian Cashmere, Wool-Cashmere Blend"
        imageUrl = "https://images.unsplash.com/photo-1593030761757-71fae45fa0e7?auto=format&fit=crop&w=800&q=80"
        active = $true
    } | ConvertTo-Json
    $updateRes = Invoke-RestMethod -Uri "$baseUrl/products/$createdProductId" -Method Put -ContentType "application/json" -Body $updateProd
    Write-Host "Updated Product BasePrice: INR $($updateRes.data.basePrice)"

    # Toggle status
    $toggleRes = Invoke-RestMethod -Uri "$baseUrl/products/$createdProductId/toggle-status" -Method Put
    Write-Host "Product Active status after toggle: $($toggleRes.data.active)"
    if ($toggleRes.data.active -ne $false) {
        throw "Expected product active to be false after toggle"
    }

    # Toggle back to true
    $toggleBack = Invoke-RestMethod -Uri "$baseUrl/products/$createdProductId/toggle-status" -Method Put
    Write-Host "Product Active status after toggle back: $($toggleBack.data.active)"
    if ($toggleBack.data.active -ne $true) {
        throw "Expected product active to be true after toggle back"
    }
}

# 4. Customer Custom Order Placement Flow
$customerUser = $null
$placedOrder = $null
Test-Endpoint "Customer Places Bespoke Order With Measurements and Instructions" {
    # Login customer
    $custLogin = @{
        email = "customer@example.com"
        password = "customer123"
    } | ConvertTo-Json
    $custRes = Invoke-RestMethod -Uri "$baseUrl/users/login" -Method Post -ContentType "application/json" -Body $custLogin
    $script:customerUser = $custRes.data
    Write-Host "Customer: $($customerUser.fullName) (ID: $($customerUser.id))"

    # Save anatomical measurement profile
    $measBody = @{
        customerId = $customerUser.id
        profileName = "Savile Row Executive Fit"
        chest = 42.0
        waist = 34.5
        hips = 41.5
        shoulderWidth = 18.5
        sleeveLength = 25.5
        shirtLength = 31.0
        inseam = 32.5
        neck = 16.5
        unit = "inches"
        notes = "Athletic build, slight forward slope shoulders, 1-inch trouser break"
    } | ConvertTo-Json
    $measRes = Invoke-RestMethod -Uri "$baseUrl/measurements" -Method Post -ContentType "application/json" -Body $measBody
    $measId = $measRes.data.id
    Write-Host "Measurement Profile Saved with ID: $measId"

    # Add to cart
    $cartItem = @{
        productId = $script:createdProductId
        quantity = 1
        selectedSize = "Custom Made-to-Measure"
        selectedColor = "Midnight Navy"
        selectedFabric = "100% Mongolian Cashmere"
        measurementId = $measId
        customizationInstructions = "Monogram 'DS' in gold silk thread on interior pocket. Horn buttons."
    } | ConvertTo-Json
    $cartRes = Invoke-RestMethod -Uri "$baseUrl/cart/$($customerUser.id)/items" -Method Post -ContentType "application/json" -Body $cartItem
    Write-Host "Garment added to cart. Grand Total: INR $($cartRes.data.grandTotal)"

    # Place order
    $orderBody = @{
        customerId = $customerUser.id
        shippingAddress = "742 Evergreen Terrace, Mumbai, Maharashtra 400001"
        contactPhone = "+91 98765 43210"
        customerNotes = "Please deliver between 2 PM and 5 PM. Signature upon delivery."
    } | ConvertTo-Json
    $orderRes = Invoke-RestMethod -Uri "$baseUrl/orders" -Method Post -ContentType "application/json" -Body $orderBody
    $script:placedOrder = $orderRes.data
    Write-Host "Order Placed Successfully! Tracking: $($placedOrder.orderTrackingNumber), Status: $($placedOrder.status)"
}

# 5. Admin Assigns Order to Tailor Marcus Vance (ID: 2)
Test-Endpoint "Admin Assigns Order to Tailor Bay" {
    $assignBody = @{
        tailorId = 2
    } | ConvertTo-Json
    $assignRes = Invoke-RestMethod -Uri "$baseUrl/orders/$($placedOrder.id)/assign-tailor" -Method Put -ContentType "application/json" -Body $assignBody
    if ($assignRes.data.status -ne "ASSIGNED" -or $assignRes.data.assignedTailorId -ne 2) {
        throw "Failed to assign order to tailor"
    }
    Write-Host "Order $($assignRes.data.orderTrackingNumber) assigned to: $($assignRes.data.assignedTailorName)"
}

# 6. Tailor Marcus Vance Logs In, Views Assigned Order & Measurements, and Updates Stitching Status
Test-Endpoint "Tailor Bay Operations: View Queue, Measurements & Update Stitching" {
    # Tailor Login
    $tailorLogin = @{
        email = "tailor.marcus@customclothing.com"
        password = "tailor123"
    } | ConvertTo-Json
    $tailorRes = Invoke-RestMethod -Uri "$baseUrl/users/login" -Method Post -ContentType "application/json" -Body $tailorLogin
    $tailorUser = $tailorRes.data
    Write-Host "Tailor: $($tailorUser.fullName) (Role: $($tailorUser.role))"

    # View assigned queue
    $queue = Invoke-RestMethod -Uri "$baseUrl/orders/tailor/$($tailorUser.id)"
    Write-Host "Tailor Bay Queue count: $($queue.data.Count)"
    $myOrder = $queue.data | Where-Object { $_.id -eq $script:placedOrder.id }
    if (-not $myOrder) {
        throw "Assigned order not found in tailor queue"
    }

    # Verify customer measurements on the item
    $firstItem = $myOrder.items[0]
    Write-Host "Item: $($firstItem.productName)"
    Write-Host "Custom Instructions: $($firstItem.customizationInstructions)"
    Write-Host "Chest: $($firstItem.measurement.chest) $($firstItem.measurement.unit)"
    Write-Host "Waist: $($firstItem.measurement.waist) $($firstItem.measurement.unit)"
    Write-Host "Shoulder: $($firstItem.measurement.shoulderWidth) $($firstItem.measurement.unit)"

    if ($firstItem.measurement.chest -ne 42.0) {
        throw "Measurements not properly attached to order item"
    }

    # Step A: Update to UNDER_STITCHING
    $stitchBody = @{
        status = "UNDER_STITCHING"
        tailorNotes = "Fabric chalked and sheared. Canvassing basted, horsehair chest canvas fitted."
    } | ConvertTo-Json
    $stitchRes = Invoke-RestMethod -Uri "$baseUrl/orders/$($myOrder.id)/tailor/$($tailorUser.id)/status" -Method Put -ContentType "application/json" -Body $stitchBody
    Write-Host "Status updated to: $($stitchRes.data.status)"
    Write-Host "Tailor Workshop Log: $($stitchRes.data.tailorNotes)"
    if ($stitchRes.data.status -ne "UNDER_STITCHING") {
        throw "Status update to UNDER_STITCHING failed"
    }

    # Step B: Update to READY_FOR_DELIVERY
    $readyBody = @{
        status = "READY_FOR_DELIVERY"
        tailorNotes = "Garment fully stitched, hand-pressed with steam, monogram verified. Ready for fitting dispatch."
    } | ConvertTo-Json
    $readyRes = Invoke-RestMethod -Uri "$baseUrl/orders/$($myOrder.id)/tailor/$($tailorUser.id)/status" -Method Put -ContentType "application/json" -Body $readyBody
    Write-Host "Status updated to: $($readyRes.data.status)"
    if ($readyRes.data.status -ne "READY_FOR_DELIVERY") {
        throw "Status update to READY_FOR_DELIVERY failed"
    }
}

# 7. Customer Tracks Order & Verifies Tailor Progress
Test-Endpoint "Customer Order Tracking Shows Tailor Status & Workshop Log" {
    $tracked = Invoke-RestMethod -Uri "$baseUrl/orders/track/$($placedOrder.orderTrackingNumber)"
    Write-Host "Tracked Status: $($tracked.data.status)"
    Write-Host "Assigned Tailor: $($tracked.data.assignedTailorName)"
    Write-Host "Workshop Log: $($tracked.data.tailorNotes)"

    if ($tracked.data.status -ne "READY_FOR_DELIVERY") {
        throw "Expected status READY_FOR_DELIVERY"
    }
    if ($tracked.data.assignedTailorName -ne "Marcus Vance") {
        throw "Expected assigned tailor Marcus Vance"
    }
    if (-not $tracked.data.tailorNotes.Contains("Ready for fitting")) {
        throw "Expected workshop log to be visible to tracking"
    }
}

# 8. Admin Marks Order DELIVERED
Test-Endpoint "Admin Updates Order to DELIVERED" {
    $delivBody = @{
        status = "DELIVERED"
        tailorNotes = "Garment hand-delivered to patron. Perfect fit confirmed."
    } | ConvertTo-Json
    $delivRes = Invoke-RestMethod -Uri "$baseUrl/orders/$($placedOrder.id)/status" -Method Put -ContentType "application/json" -Body $delivBody
    Write-Host "Final Order Status: $($delivRes.data.status)"
    if ($delivRes.data.status -ne "DELIVERED") {
        throw "Expected status DELIVERED"
    }
}

Write-Host "`n==========================================" -ForegroundColor Green
Write-Host "ALL ADMIN AND TAILOR FLOW TESTS COMPLETED SUCCESSFULLY!" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Green
