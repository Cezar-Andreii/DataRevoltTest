/**
 * Service pentru generarea codului JavaScript (client-side fallback)
 * Migrează logica din TaggingController.generateJavaScriptCode()
 */

/**
 * Generează codul JavaScript pentru un eveniment și parametrii săi
 * @param {string} eventName - Numele evenimentului
 * @param {Array<string>} selectedParameters - Lista de parametri selectați
 * @param {Array<string>} selectedItems - Lista de item-uri selectate (pentru items array)
 * @returns {string} Codul JavaScript generat
 */
export const generateJavaScriptCode = (eventName, selectedParameters, selectedItems) => {
  if (!eventName) {
    return '';
  }

  const code = [];
  
  // Header standard - formatat frumos
  code.push("window.dataLayer = window.dataLayer || [];");
  code.push("dataLayer.push({ 'ecommerce': null });");
  code.push("dataLayer.push({");
  code.push(`  'event': '${eventName}'`);
  
  // Adaugă parametrii selectați doar dacă există
  if (selectedParameters && selectedParameters.length > 0) {
    code.push("  'ecommerce': {");
    
    const paramLines = [];
    selectedParameters.forEach((paramName, index) => {
      if (index > 0) {
        paramLines.push("");
      }
      
      switch (paramName) {
        case "currency":
          paramLines.push("    'currency': $value");
          break;
        case "value":
          paramLines.push("    'value': $value");
          break;
        case "customer_type":
          paramLines.push("    'customer_type': $value");
          break;
        case "transaction_id":
          paramLines.push("    'transaction_id': $value");
          break;
        case "coupon":
          paramLines.push("    'coupon': $value");
          break;
        case "shipping":
          paramLines.push("    'shipping': $value");
          break;
        case "tax":
          paramLines.push("    'tax': $value");
          break;
        case "affiliation":
          paramLines.push("    'affiliation': $value");
          break;
        case "search_term":
          paramLines.push("    'search_term': $value");
          break;
        case "method":
          paramLines.push("    'method': $value");
          break;
        case "items":
          paramLines.push("    'items': [");
          paramLines.push("      {");
          
          // Adaugă opțiunile selectate pentru items
          if (selectedItems && selectedItems.length > 0) {
            const itemLines = [];
            selectedItems.forEach((item, itemIndex) => {
              if (itemIndex > 0) {
                itemLines.push(",");
              }
              
              switch (item) {
                case "item_id":
                  itemLines.push("        'item_id': $value");
                  break;
                case "item_name":
                  itemLines.push("        'item_name': $value");
                  break;
                case "item_category":
                  itemLines.push("        'item_category': $value");
                  break;
                case "item_category2":
                  itemLines.push("        'item_category2': $value");
                  break;
                case "item_category3":
                  itemLines.push("        'item_category3': $value");
                  break;
                case "item_brand":
                  itemLines.push("        'item_brand': $value");
                  break;
                case "item_variant":
                  itemLines.push("        'item_variant': $value");
                  break;
                case "item_list_id":
                  itemLines.push("        'item_list_id': $value");
                  break;
                case "item_list_name":
                  itemLines.push("        'item_list_name': $value");
                  break;
                case "price":
                  itemLines.push("        'price': $value");
                  break;
                case "quantity":
                  itemLines.push("        'quantity': $value");
                  break;
                case "discount":
                  itemLines.push("        'discount': $value");
                  break;
                case "affiliation":
                  itemLines.push("        'affiliation': $value");
                  break;
                case "coupon":
                  itemLines.push("        'coupon': $value");
                  break;
                case "index":
                  itemLines.push("        'index': $value");
                  break;
                default:
                  itemLines.push(`        '${item}': $value`);
                  break;
              }
            });
            paramLines.push(...itemLines);
          } else {
            // Default: item_id, item_name, item_category
            paramLines.push("        'item_id': $value,");
            paramLines.push("        'item_name': $value,");
            paramLines.push("        'item_category': $value");
          }
          
          paramLines.push("");
          paramLines.push("      }");
          paramLines.push("    ]");
          break;
        default:
          paramLines.push(`    '${paramName}': $value`);
          break;
      }
    });
    
    code.push(...paramLines);
    code.push("  }");
  }
  
  // Footer - formatat frumos
  code.push("});");
  
  return code.join("\n");
};

/**
 * Generează codul JavaScript pentru export (o singură linie)
 * @param {string} eventName - Numele evenimentului
 * @param {Array<string>} selectedParameters - Lista de parametri selectați
 * @returns {string} Codul JavaScript generat (pe o singură linie)
 */
export const generateJavaScriptCodeForExport = (eventName, selectedParameters) => {
  if (!eventName) {
    return '';
  }

  const code = [];
  
  // Header standard - pe o singură linie
  code.push("window.dataLayer = window.dataLayer || [];");
  code.push("dataLayer.push({ 'ecommerce': null });");
  code.push("dataLayer.push({");
  code.push(`'event': '${eventName}'`);
  
  // Adaugă parametrii selectați doar dacă există
  if (selectedParameters && selectedParameters.length > 0) {
    code.push(", 'ecommerce': {");
    
    const paramParts = [];
    selectedParameters.forEach((paramName) => {
      switch (paramName) {
        case "currency":
          paramParts.push("'currency': $value");
          break;
        case "value":
          paramParts.push("'value': $value");
          break;
        case "customer_type":
          paramParts.push("'customer_type': $value");
          break;
        case "transaction_id":
          paramParts.push("'transaction_id': $value");
          break;
        case "coupon":
          paramParts.push("'coupon': $value");
          break;
        case "shipping":
          paramParts.push("'shipping': $value");
          break;
        case "tax":
          paramParts.push("'tax': $value");
          break;
        case "affiliation":
          paramParts.push("'affiliation': $value");
          break;
        case "search_term":
          paramParts.push("'search_term': $value");
          break;
        case "method":
          paramParts.push("'method': $value");
          break;
        case "items":
          paramParts.push("'items': [{ item_object_1 }]");
          break;
        default:
          paramParts.push(`'${paramName}': $value`);
          break;
      }
    });
    
    code.push(paramParts.join(", "));
    code.push(" }");
  }
  
  // Footer - pe o singură linie
  code.push(" });");
  
  return code.join(" ");
};

export default {
  generateJavaScriptCode,
  generateJavaScriptCodeForExport,
};

