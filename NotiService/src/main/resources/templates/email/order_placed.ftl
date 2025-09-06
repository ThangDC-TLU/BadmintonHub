<!doctype html>
<html lang="${order.locale! 'vi-VN'}">
<head>
    <meta charset="utf-8">
    <meta name="x-apple-disable-message-reformatting">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Order ${order.orderCode} Confirmed</title>
    <style>
        /* Một chút responsive cơ bản */
        @media only screen and (max-width:600px){
            .container{width:100% !important; padding:16px !important;}
            .stack{display:block !important; width:100% !important;}
            .text-right{ text-align:left !important; }
            .btn{display:block !important; width:100% !important;}
        }
        /* Fix cho dark mode (nhiều client hỗ trợ) */
        @media (prefers-color-scheme: dark) {
            body{background:#0b0b0b !important; color:#eaeaea !important;}
            .card{background:#161616 !important; border-color:#2a2a2a !important;}
            .border{border-color:#2a2a2a !important;}
        }
    </style>
</head>
<body style="margin:0; padding:0; background:#f4f5f7; font-family:Arial,Helvetica,sans-serif; color:#1f2937;">
<table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="background:#f4f5f7;">
    <tr>
        <td align="center" style="padding:24px;">
            <table class="container" role="presentation" width="600" cellspacing="0" cellpadding="0" style="width:600px; background:#ffffff; border-radius:12px; overflow:hidden; box-shadow:0 2px 8px rgba(0,0,0,.05);">
                <!-- Hero -->
                <tr>
                    <td style="padding:28px 24px 12px;">
                        <h1 style="margin:0 0 8px; font-size:22px; color:#111827;">Cảm ơn ${order.customerName! 'bạn'}!</h1>
                        <p style="margin:0; font-size:14px; color:#374151;">
                            Đơn hàng <strong>#${order.orderCode}</strong> của bạn đã được tạo thành công.
                        </p>
                        <p style="margin:8px 0 0; font-size:12px; color:#6b7280;">
                            Thời gian: <strong>${createdAtFormatted!'--/--/---- --:--'}</strong> (${order.timezone!'Asia/Ho_Chi_Minh'})
                        </p>
                    </td>
                </tr>

                <!-- Order summary card -->
                <tr>
                    <td style="padding:0 24px 4px;">
                        <table class="card" width="100%" role="presentation" cellspacing="0" cellpadding="0" style="background:#ffffff; border:1px solid #e5e7eb; border-radius:10px;">
                            <tr>
                                <td style="padding:16px 16px 4px;">
                                    <table width="100%" role="presentation">
                                        <tr>
                                            <td style="font-size:14px; color:#374151;">
                                                Phương thức thanh toán:
                                                <strong>${order.paymentMethod! '—'}</strong>
                                                <#if order.paymentStatus??>
                                                <span style="display:inline-block; padding:2px 8px; margin-left:6px; font-size:12px; border-radius:999px; background:#ecfdf5; color:#065f46; border:1px solid #a7f3d0;">
                              ${order.paymentStatus}
                            </span>
                                            </#if>
                                </td>
                                <td align="right" class="text-right" style="font-size:14px; color:#374151;">
                                    Mã đơn: <strong>#${order.orderCode}</strong>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>

                <!-- Items table -->
                <tr>
                    <td style="padding:8px 16px 8px;">
                        <table width="100%" role="presentation" cellspacing="0" cellpadding="0" style="border-collapse:collapse;">
                            <tr style="background:#f9fafb;">
                                <th align="left" style="padding:10px; font-size:12px; color:#6b7280; text-transform:uppercase; letter-spacing:.04em;">Sản phẩm</th>
                                <th align="center" style="padding:10px; font-size:12px; color:#6b7280; text-transform:uppercase;">SL</th>
                                <th align="right" style="padding:10px; font-size:12px; color:#6b7280; text-transform:uppercase;">Đơn giá</th>
                                <th align="right" style="padding:10px; font-size:12px; color:#6b7280; text-transform:uppercase;">Thành tiền</th>
                            </tr>

                            <#list items as it>
                            <tr class="border">
                                <td class="stack" style="padding:10px; border-bottom:1px solid #e5e7eb;">
                                    <table role="presentation">
                                        <tr>
                                            <td style="vertical-align:top;">
                                                <img src="${it.image!'https://via.placeholder.com/64'}" alt="" width="64" height="64" style="border-radius:8px; display:block; margin-right:10px;">
                                            </td>
                                            <td style="padding-left:10px; vertical-align:top; font-size:14px; color:#111827;">
                                                <div style="font-weight:bold;">${it.name}</div>
                                                <div style="font-size:12px; color:#6b7280;">${it.optionLabel!'—'}</div>
                                                <div style="font-size:12px; color:#6b7280;">Mã SP: ${it.productId!''}<#if it.optionId??> / Option: ${it.optionId}</#if></div>
                                            </td>
                                        </tr>
                                    </table>
                                </td>
                                <td align="center" style="padding:10px; font-size:14px; border-bottom:1px solid #e5e7eb;">${it.quantity}</td>
                                <td align="right" style="padding:10px; font-size:14px; border-bottom:1px solid #e5e7eb;">${it.unitPriceFormatted! it.unitPrice?string}</td>
                                <td align="right" style="padding:10px; font-size:14px; border-bottom:1px solid #e5e7eb;"><strong>${it.lineTotalFormatted! it.lineTotal?string}</strong></td>
                            </tr>
                        </#list>
            </table>
        </td>
    </tr>

    <!-- Totals -->
    <tr>
        <td style="padding:8px 16px 16px;">
            <table role="presentation" width="100%">
                <tr>
                    <td class="stack" style="width:55%;"></td>
                    <td class="stack" style="width:45%;">
                        <table role="presentation" width="100%">
                            <tr>
                                <td style="padding:6px 0; font-size:14px; color:#6b7280;">Tạm tính</td>
                                <td align="right" style="padding:6px 0; font-size:14px; color:#111827;">${subtotalFormatted! order.subtotal?string}</td>
                            </tr>
                            <#if order.discountTotal??>
                            <tr>
                                <td style="padding:6px 0; font-size:14px; color:#6b7280;">Giảm giá</td>
                                <td align="right" style="padding:6px 0; font-size:14px; color:#111827;">- ${discountTotalFormatted! order.discountTotal?string}</td>
                            </tr>
                        </#if>
                <tr>
                    <td style="padding:6px 0; font-size:14px; color:#6b7280;">Phí vận chuyển</td>
                    <td align="right" style="padding:6px 0; font-size:14px; color:#111827;">${shippingFeeFormatted! order.shippingFee?string}</td>
                </tr>
                <#if order.taxTotal??>
                <tr>
                    <td style="padding:6px 0; font-size:14px; color:#6b7280;">Thuế</td>
                    <td align="right" style="padding:6px 0; font-size:14px; color:#111827;">${taxTotalFormatted! order.taxTotal?string}</td>
                </tr>
            </#if>
    <tr>
        <td style="padding:8px 0; font-size:16px; color:#111827; font-weight:bold; border-top:1px solid #e5e7eb;">Tổng thanh toán</td>
        <td align="right" style="padding:8px 0; font-size:18px; color:#111827; font-weight:bold; border-top:1px solid #e5e7eb;">
            ${grandTotalFormatted! order.grandTotal?string} ${currencySymbol! ''}
        </td>
    </tr>
</table>
</td>
</tr>
</table>
</td>
</tr>

</table>
</td>
</tr>

<!-- Shipping & actions -->
<tr>
    <td style="padding:8px 24px 24px;">
        <table width="100%" role="presentation">
            <tr>
                <td class="stack" style="width:60%; vertical-align:top;">
                    <div style="padding:12px; border:1px solid #e5e7eb; border-radius:10px;">
                        <div style="font-size:14px; color:#374151; margin-bottom:6px;"><strong>Thông tin giao hàng</strong></div>
                        <div style="font-size:13px; color:#111827;">${order.shippingAddress.name!'—'} (${order.shippingAddress.phone!'—'})</div>
                        <div style="font-size:13px; color:#6b7280;">${order.shippingAddress.oneLine!'—'}</div>
                    </div>
                </td>
                <td class="stack" style="width:40%; vertical-align:top;" align="right">
                    <a class="btn" href="${order.orderDetailUrl!'#'}" style="text-decoration:none; display:inline-block; padding:12px 18px; margin:4px 0; background:#0ea5e9; color:#ffffff; border-radius:8px; font-weight:bold;">Xem chi tiết đơn</a><br>
                    <#if order.trackingUrl??>
                    <a class="btn" href="${order.trackingUrl}" style="text-decoration:none; display:inline-block; padding:12px 18px; margin:4px 0; background:#111827; color:#ffffff; border-radius:8px; font-weight:bold;">Theo dõi vận chuyển</a>
                </#if>
                <#if order.approvalUrl?? && order.paymentStatus?string('')?upper_case == 'PENDING'>
                <a class="btn" href="${order.approvalUrl}" style="text-decoration:none; display:inline-block; padding:12px 18px; margin:4px 0; background:#16a34a; color:#ffffff; border-radius:8px; font-weight:bold;">Thanh toán/Approve</a>
            </#if>
    </td>
</tr>
</table>
</td>
</tr>

<!-- Footer -->
<tr>
    <td style="padding:16px 24px 28px; font-size:12px; color:#6b7280;">
        Nếu cần hỗ trợ, vui lòng liên hệ <a href="mailto:${supportEmail!'support@badmintonhub.com'}" style="color:#0ea5e9; text-decoration:none;">${supportEmail!'support@badmintonhub.com'}</a>.<br>
        © ${.now?string('yyyy')} ${companyName!'BadmintonHub'}. All rights reserved.
    </td>
</tr>

</table>
</td>
</tr>
</table>
</body>
</html>
