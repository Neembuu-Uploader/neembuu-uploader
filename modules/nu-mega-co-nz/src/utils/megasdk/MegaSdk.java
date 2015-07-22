/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils.megasdk;

import java.util.Random;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import java.lang.Math;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author RD
 */
public class MegaSdk {
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String responseString;
    
    private String sid = "";
    
    Random rand = new Random();
    private int seq_no = rand.nextInt((0xFFFFFFFF - 0) + 1) + 0;
    
    public String api_req(String request) {
        
    }
    
    public String[] array_merge (String[] first, String[] second) {
        List<String> both = new ArrayList<String>(first.length + second.length);
        Collections.addAll(both, first);
        Collections.addAll(both, second);
        return both.toArray(new String[both.size()]);
    }
    
    public String a32_to_str (int[] hex) {
        // i think hex and other arrays could also be byte[]
        
        //return call_user_func_array('pack', array_merge(array('N*'), $hex));
        int[]r = new int[hex.length + 2];
        r[0] = 'N'; r[1]='*';
        System.arraycopy(hex, 0, r, 2, hex.length);
        
        String ret = "";
        for (int i = 0; i < r.length; i++) {
            ret+=packN(r[i]);
        }
        return ret;
    }
    
    // from https://gist.github.com/enrobsop/8403667
    static String packN(int value) {
        byte[]bytes = ByteBuffer.allocate(4).putInt(value).array();
        char[]cbytes = toPositiveByteArray(bytes);
        return new String(cbytes);
    }
    
    static char[] toPositiveByteArray(byte[]bytes) {
        char[]c = new char[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            byte it = bytes[i];
            c[i] = it<0?((char)(256+it)):(char)it;
        }
        return c;
    } 
    
    public int[] str_to_a32(String b) {
        double str_len = (double) b.length();
        double mult = 4.0;
        double rounded_val = Math.ceil(str_len / mult);
        
        int pad_length = ((int) rounded_val) * 4;
        String pad_with = "\0";
        // Add padding, we need a string with a length multiple of 4
        b = str_pad(b, pad_length, pad_with);
        return unpack(b.getBytes());
    }
    
    public String str_pad(String str, Integer length, String pad) { // str_pad("Hi", 10, 'R') //gives "HiRRRRRRRR"
      return String.format("%" + (length - str.length()) + "s", "")
                   .replace(" ", String.valueOf(pad)) 
             +
             str;
    }
    
    public static int[] unpack ( byte[] bytes ) {
        // first, wrap the input array in a ByteBuffer:
        ByteBuffer byteBuf = ByteBuffer.wrap( bytes );

        // then turn it into an IntBuffer, using big-endian ("Network") byte order:
        byteBuf.order( ByteOrder.BIG_ENDIAN );
        IntBuffer intBuf = byteBuf.asIntBuffer();

        // finally, dump the contents of the IntBuffer into an array
        int[] integers = new int[ intBuf.remaining() ];
        intBuf.get( integers );
        return integers;
    }
    
    public String stringhash (String s, String aeskey) {
        int[] s32 = str_to_a32(s);
        int[] h32 = {0, 0, 0, 0};
        
        int i = 0;
        for(i=0 ; i<s32.length; i++){
            h32[i%4] ^= s32[i];
        }
        
        for (i=0; i<0x4000; i++) {
            h32 = aes_cbc_encrypt_a32(h32, aeskey);
        }
        
        
    }
    
    /*
    
    ------------------------------------------------------------
    --------This is the PHP code that needs to be ported--------
    ------------------------------------------------------------
    
<?php
$sid = '';
$seqno = rand(0, 0xFFFFFFFF);
 
$master_key = '';
$rsa_priv_key = '';
 
function base64urldecode($data) {
  $data .= substr('==', (2 - strlen($data) * 3) % 4);
  $data = str_replace(array('-', '_', ','), array('+', '/', ''), $data);
  return base64_decode($data);  
}
 
function base64urlencode($data) {
  return str_replace(array('+', '/', '='), array('-', '_', ''), base64_encode($data));
}
 
function a32_to_str($hex) {
  return call_user_func_array('pack', array_merge(array('N*'), $hex));
}
 
function a32_to_base64($a) {
  return base64urlencode(a32_to_str($a));
}
 
function str_to_a32($b) {
  // Add padding, we need a string with a length multiple of 4
  $b = str_pad($b, 4 * ceil(strlen($b) / 4), "\0");
  return array_values(unpack('N*', $b));
}
 
function base64_to_a32($s) {
  return str_to_a32(base64urldecode($s));
}
 
function aes_cbc_encrypt($data, $key) {
  return mcrypt_encrypt(MCRYPT_RIJNDAEL_128, $key, $data, MCRYPT_MODE_CBC, "\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0");
}
 
function aes_cbc_decrypt($data, $key) {
  return mcrypt_decrypt(MCRYPT_RIJNDAEL_128, $key, $data, MCRYPT_MODE_CBC, "\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0");
}
 
function aes_cbc_encrypt_a32($data, $key) {
  return str_to_a32(aes_cbc_encrypt(a32_to_str($data), a32_to_str($key)));
}
 
function aes_cbc_decrypt_a32($data, $key) {
  return str_to_a32(aes_cbc_decrypt(a32_to_str($data), a32_to_str($key)));
}
 
function aes_ctr_encrypt($data, $key, $iv) {
  return mcrypt_encrypt(MCRYPT_RIJNDAEL_128, $key, $data, 'ctr', $iv);
}
 
function aes_ctr_decrypt($data, $key, $iv) {
  return mcrypt_decrypt(MCRYPT_RIJNDAEL_128, $key, $data, 'ctr', $iv);
}
 
 // BEGIN RSA-related stuff -- taken from PEAR Crypt_RSA package
 // http://pear.php.net/package/Crypt_RSA
    
function bin2int($str) {
  $result = 0;
  $n = strlen($str);
  do {
    $result = bcadd(bcmul($result, 256), ord($str[--$n]));
  } while ($n > 0);
  return $result;
}
 
function int2bin($num) {
  $result = '';
  do {
    $result .= chr(bcmod($num, 256));
    $num = bcdiv($num, 256);
  } while (bccomp($num, 0));
  return $result;
}
 
function bitOr($num1, $num2, $start_pos) {
  $start_byte = intval($start_pos / 8);
  $start_bit = $start_pos % 8;
  $tmp1 = int2bin($num1);
 
  $num2 = bcmul($num2, 1 << $start_bit);
  $tmp2 = int2bin($num2);
  if ($start_byte < strlen($tmp1)) {
    $tmp2 |= substr($tmp1, $start_byte);
    $tmp1 = substr($tmp1, 0, $start_byte) . $tmp2;
  } else {
    $tmp1 = str_pad($tmp1, $start_byte, '\0') . $tmp2;
  }
  return bin2int($tmp1);
}
 
function bitLen($num) {
  $tmp = int2bin($num);
  $bit_len = strlen($tmp) * 8;
  $tmp = ord($tmp[strlen($tmp) - 1]);
  if (!$tmp) {
    $bit_len -= 8;
  } else {
    while (!($tmp & 0x80)) {
      $bit_len--;
      $tmp <<= 1;
    }
  }
  return $bit_len;
}
 
function rsa_decrypt($enc_data, $p, $q, $d) {
  $enc_data = int2bin($enc_data);
  $exp = $d;
  $modulus = bcmul($p, $q);
  $data_len = strlen($enc_data);
  $chunk_len = bitLen($modulus) - 1;
  $block_len = (int) ceil($chunk_len / 8);
  $curr_pos = 0;
  $bit_pos = 0;
  $plain_data = 0;
  while ($curr_pos < $data_len) {
    $tmp = bin2int(substr($enc_data, $curr_pos, $block_len));
    $tmp = bcpowmod($tmp, $exp, $modulus);
    $plain_data = bitOr($plain_data, $tmp, $bit_pos);
    $bit_pos += $chunk_len;
    $curr_pos += $block_len;
  }
  return int2bin($plain_data);
}

 // END RSA-related stuff
 
function stringhash($s, $aeskey) {
  $s32 = str_to_a32($s);
  $h32 = array(0, 0, 0, 0);
 
  for ($i = 0; $i < count($s32); $i++) {
    $h32[$i % 4] ^= $s32[$i];
  }
 
  for ($i = 0; $i < 0x4000; $i++) {
    $h32 = aes_cbc_encrypt_a32($h32, $aeskey);
  }
 
  return a32_to_base64(array($h32[0], $h32[2]));
}
 
function prepare_key($a) {
  $pkey = array(0x93C467E3, 0x7DB0C7A4, 0xD1BE3F81, 0x0152CB56);
 
  for ($r = 0; $r < 0x10000; $r++) {
    for ($j = 0; $j < count($a); $j += 4) {
      $key = array(0, 0, 0, 0);
 
      for ($i = 0; $i < 4; $i++) {
        if ($i + $j < count($a)) {
          $key[$i] = $a[$i + $j];
        }
      }
 
      $pkey = aes_cbc_encrypt_a32($pkey, $key);
    }
  }
 
  return $pkey;
}
 
function encrypt_key($a, $key) {
  $x = array();
 
  for ($i = 0; $i < count($a); $i += 4) {
    $x = array_merge($x, aes_cbc_encrypt_a32(array_slice($a, $i, 4), $key));
  }
 
  return $x;
}
 
function decrypt_key($a, $key) {
  $x = array();
 
  for ($i = 0; $i < count($a); $i += 4) {
    $x = array_merge($x, aes_cbc_decrypt_a32(array_slice($a, $i, 4), $key));
  }
 
  return $x;
}
 
function mpi2bc($s) {
  $s = bin2hex(substr($s, 2));
  $len = strlen($s);
  $n = 0;
  for ($i = 0; $i < $len; $i++) {
    $n = bcadd($n, bcmul(hexdec($s[$i]), bcpow(16, $len - $i - 1)));
  }
  return $n;
}
 
function api_req($req) {
  global $seqno, $sid;
  $resp = post('https://g.api.mega.co.nz/cs?id=' . ($seqno++) . ($sid ? '&sid=' . $sid : ''), json_encode(array($req)));
  $resp = json_decode($resp);
  return $resp[0];
}
 
function post($url, $data) {
  $ch = curl_init($url);
  curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
  curl_setopt($ch, CURLOPT_POST, true);
  curl_setopt($ch, CURLOPT_POSTFIELDS, $data);
  $resp = curl_exec($ch);
  curl_close($ch);
  return $resp;
}
 
function login($email, $password) {
  global $sid, $master_key, $rsa_priv_key;
  $password_aes = prepare_key(str_to_a32($password));
  $uh = stringhash(strtolower($email), $password_aes);
  $res = api_req(array('a' => 'us', 'user' => $email, 'uh' => $uh));
 
  $enc_master_key = base64_to_a32($res->k);
  $master_key = decrypt_key($enc_master_key, $password_aes);
  if (!empty($res->csid)) {
    $enc_rsa_priv_key = base64_to_a32($res->privk);
    $rsa_priv_key = decrypt_key($enc_rsa_priv_key, $master_key);
 
    $privk = a32_to_str($rsa_priv_key);
    $rsa_priv_key = array(0, 0, 0, 0);
 
    for ($i = 0; $i < 4; $i++) {
      $l = ((ord($privk[0]) * 256 + ord($privk[1]) + 7) / 8) + 2;
      $rsa_priv_key[$i] = mpi2bc(substr($privk, 0, $l));
      $privk = substr($privk, $l);
    }
 
    $enc_sid = mpi2bc(base64urldecode($res->csid));
    $sid = rsa_decrypt($enc_sid, $rsa_priv_key[0], $rsa_priv_key[1], $rsa_priv_key[2]);
    $sid = base64urlencode(substr(strrev($sid), 0, 43));
  }
}
 
function enc_attr($attr, $key) {
  $attr = 'MEGA' . json_encode($attr);
  return aes_cbc_encrypt($attr, a32_to_str($key));
}
 
function dec_attr($attr, $key) {
  $attr = trim(aes_cbc_decrypt($attr, a32_to_str($key)));
  if (substr($attr, 0, 6) != 'MEGA{"') {
    return false;
  }
  return json_decode(substr($attr, 4));
}
 
function get_chunks($size) {
  $chunks = array();
  $p = $pp = 0;
 
  for ($i = 1; $i <= 8 && $p < $size - $i * 0x20000; $i++) {
    $chunks[$p] = $i * 0x20000;
    $pp = $p;
    $p += $chunks[$p];
  }
 
  while ($p < $size) {
    $chunks[$p] = 0x100000;
    $pp = $p;
    $p += $chunks[$p];
  }
 
  $chunks[$pp] = ($size - $pp);
  if (!$chunks[$pp]) {
    unset($chunks[$pp]);
  }
 
  return $chunks;
}
 
function cbc_mac($data, $k, $n) {
  $padding_size = (strlen($data) % 16) == 0 ? 0 : 16 - strlen($data) % 16;
  $data .= str_repeat("\0", $padding_size);
 
  $chunks = get_chunks(strlen($data));
  $file_mac = array(0, 0, 0, 0);
 
  foreach ($chunks as $pos => $size) {
    $chunk_mac = array($n[0], $n[1], $n[0], $n[1]);
    for ($i = $pos; $i < $pos + $size; $i += 16) {
      $block = str_to_a32(substr($data, $i, 16));
      $chunk_mac = array($chunk_mac[0] ^ $block[0], $chunk_mac[1] ^ $block[1], $chunk_mac[2] ^ $block[2], $chunk_mac[3] ^ $block[3]);
      $chunk_mac = aes_cbc_encrypt_a32($chunk_mac, $k);
    }
    $file_mac = array($file_mac[0] ^ $chunk_mac[0], $file_mac[1] ^ $chunk_mac[1], $file_mac[2] ^ $chunk_mac[2], $file_mac[3] ^ $chunk_mac[3]);
    $file_mac = aes_cbc_encrypt_a32($file_mac, $k);
  }
 
  return $file_mac;
}
 
function uploadfile($filename) {
  global $master_key, $root_id;
 
  $data = file_get_contents($filename);
  $size = strlen($data);
  $ul_url = api_req(array('a' => 'u', 's' => $size));
  $ul_url = $ul_url->p;
 
  $ul_key = array(0, 1, 2, 3, 4, 5);
  for ($i = 0; $i < 6; $i++) {
    $ul_key[$i] = rand(0, 0xFFFFFFFF);
  }
 
  $data_crypted = aes_ctr_encrypt($data, a32_to_str(array_slice($ul_key, 0, 4)), a32_to_str(array($ul_key[4], $ul_key[5], 0, 0)));
  $completion_handle = post($ul_url, $data_crypted);
 
  $data_mac = cbc_mac($data, array_slice($ul_key, 0, 4), array_slice($ul_key, 4, 2));
  $meta_mac = array($data_mac[0] ^ $data_mac[1], $data_mac[2] ^ $data_mac[3]);
  $attributes = array('n' => basename($filename));
  $enc_attributes = enc_attr($attributes, array_slice($ul_key, 0, 4));
  $key = array($ul_key[0] ^ $ul_key[4], $ul_key[1] ^ $ul_key[5], $ul_key[2] ^ $meta_mac[0], $ul_key[3] ^ $meta_mac[1], $ul_key[4], $ul_key[5], $meta_mac[0], $meta_mac[1]);
  return api_req(array('a' => 'p', 't' => $root_id, 'n' => array(array('h' => $completion_handle, 't' => 0, 'a' => base64urlencode($enc_attributes), 'k' => a32_to_base64(encrypt_key($key, $master_key))))));
}
 
function downloadfile($file, $attributes, $k, $iv, $meta_mac) {
  $dl_url = api_req(array('a' => 'g', 'g' => 1, 'n' => $file->h));
 
  $data_enc = file_get_contents($dl_url->g);
  $data = aes_ctr_decrypt($data_enc, a32_to_str($k), a32_to_str($iv));
  file_put_contents($attributes->n, $data);
 
  $file_mac = cbc_mac($data, $k, $iv);
  if (array($file_mac[0] ^ $file_mac[1], $file_mac[2] ^ $file_mac[3]) != $meta_mac) {
    echo "MAC mismatch";
  }
}
 
function getfiles() {
  global $master_key, $root_id, $inbox_id, $trashbin_id;
 
  $files = api_req(array('a' => 'f', 'c' => 1));
  foreach ($files->f as $file) {
    if ($file->t == 0 || $file->t == 1) {
      $key = substr($file->k, strpos($file->k, ':') + 1);
      $key = decrypt_key(base64_to_a32($key), $master_key);
      if ($file->t == 0) {
        $k = array($key[0] ^ $key[4], $key[1] ^ $key[5], $key[2] ^ $key[6], $key[3] ^ $key[7]);
        $iv = array_merge(array_slice($key, 4, 2), array(0, 0));
        $meta_mac = array_slice($key, 6, 2);
      } else {
        $k = $key;
      }
      $attributes = base64urldecode($file->a);
      $attributes = dec_attr($attributes, $k);
      if ($file->h == 'gldU3Tab') {
        downloadfile($file, $attributes, $k, $iv, $meta_mac);
      }
    } else if ($file->t == 2) {
      $root_id = $file->k;
    } else if ($file->t == 3) {
      $inbox_id = $file->k;
    } else if ($file->t == 4) {
      $trashbin_id = $file->k;
    }
  }
}

?>
    */
}
