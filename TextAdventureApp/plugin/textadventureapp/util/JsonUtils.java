package plugin.textadventureapp.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * JSON文字列と Java オブジェクト間の変換処理を共通化するユーティリティクラス。
 *
 * ObjectMapper を一元管理し、
 * セーブデータ・イベントフラグ・アイテム一覧などの
 * JSON変換処理を統一的に扱う。
 *
 * 不正なJSONや変換失敗時には、
 * 空の Map / List や "{}" を返却し、
 * アプリ全体が停止しないよう安全側へフォールバックする。
 */
@Component
@Slf4j
public class JsonUtils {

  private final ObjectMapper objectMapper;

  /**
   * JsonUtils のコンストラクタ。
   * @param objectMapper JSON変換に使用する ObjectMapper
   */
  public JsonUtils(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }


  /**
   * JSON文字列をイベントフラグ用の Map<String, Boolean> に変換する。
   *
   * 主に save_data.flags や player_flags に保存された
   * JSONデータを読み込み、アプリ内で扱いやすい Map形式へ変換するために使用する。
   *
   * JSONが null・空文字・不正形式の場合は空Mapを返す。
   * また、二重JSON化されたデータにも対応している。
   *
   * @param json 変換対象のJSON文字列
   * @return イベントフラグを格納した Map。変換失敗時は空の Map を返す
   */
  public Map<String, Boolean> toFlagMap(String json) {

    if (json == null || json.isBlank()) {
      return new HashMap<>();
    }

    try {
      Object parsed = objectMapper.readValue(json, Object.class);

      // 二重JSON対策
      if (parsed instanceof String str) {
        log.warn("Double encoded JSON detected: {}", str);
        parsed = objectMapper.readValue(str, Object.class);
      }

      if (!(parsed instanceof Map<?, ?> map)) {
        log.warn("JSON is not Map: {}", parsed);
        return new HashMap<>();
      }

      Map<String, Boolean> result = new HashMap<>();

      for (Map.Entry<?, ?> entry : map.entrySet()) {

        String key = String.valueOf(entry.getKey());

        Boolean value = Boolean.TRUE.equals(entry.getValue());

        result.put(key, value);
      }

      return result;

    } catch (Exception e) {
      log.error("Failed to parse flag JSON: {}", json, e);
      return new HashMap<>();
    }
  }

  /**
   * JSON文字列を List<String> に変換する。
   *
   * 主にセーブデータ内のアイテム一覧（items）をJava の List形式へ変換するために使用する。
   * JSONが null・空文字・不正形式の場合は空Listを返す。
   *
   * @param json 変換対象のJSON文字列
   * @return 文字列リスト。変換失敗時は空の List を返す
   */
  public List<String> toStringList(String json) {

    if (json == null || json.isBlank()) {
      return new ArrayList<>();
    }

    try {
      return objectMapper.readValue(
          json,
          new TypeReference<List<String>>() {}
      );

    } catch (Exception e) {
      log.error("Failed to parse list JSON: {}", json, e);
      return new ArrayList<>();
    }
  }

  /**
   *  JavaオブジェクトをJSON文字列へ変換する。
   *  セーブデータやイベントフラグなどをDB保存用のJSON形式へ変換する際に使用する。
   *
   *  null が渡された場合は "{}" を返す。JSON変換に失敗した場合も "{}" を返却し、
   *  アプリケーション停止を防止する。
   *
   * @param obj JSONへ変換する対象オブジェクト
   * @return JSON文字列。変換失敗時は "{}" を返す
   */
  public String toJson(Object obj) {

    if (obj == null) {
      return "{}";
    }

    try {
      return objectMapper.writeValueAsString(obj);

    } catch (Exception e) {
      log.error("Failed to convert to JSON: {}", obj, e);
      return "{}";
    }
  }
}