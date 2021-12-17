/*
 * The MIT License
 *
 * Copyright 2015 Kamnev Georgiy (nt.gocha@gmail.com).
 *
 * Данная лицензия разрешает, безвозмездно, лицам, получившим копию данного программного
 * обеспечения и сопутствующей документации (в дальнейшем именуемыми "Программное Обеспечение"),
 * использовать Программное Обеспечение без ограничений, включая неограниченное право на
 * использование, копирование, изменение, объединение, публикацию, распространение, сублицензирование
 * и/или продажу копий Программного Обеспечения, также как и лицам, которым предоставляется
 * данное Программное Обеспечение, при соблюдении следующих условий:
 *
 * Вышеупомянутый копирайт и данные условия должны быть включены во все копии
 * или значимые части данного Программного Обеспечения.
 *
 * ДАННОЕ ПРОГРАММНОЕ ОБЕСПЕЧЕНИЕ ПРЕДОСТАВЛЯЕТСЯ «КАК ЕСТЬ», БЕЗ ЛЮБОГО ВИДА ГАРАНТИЙ,
 * ЯВНО ВЫРАЖЕННЫХ ИЛИ ПОДРАЗУМЕВАЕМЫХ, ВКЛЮЧАЯ, НО НЕ ОГРАНИЧИВАЯСЬ ГАРАНТИЯМИ ТОВАРНОЙ ПРИГОДНОСТИ,
 * СООТВЕТСТВИЯ ПО ЕГО КОНКРЕТНОМУ НАЗНАЧЕНИЮ И НЕНАРУШЕНИЯ ПРАВ. НИ В КАКОМ СЛУЧАЕ АВТОРЫ
 * ИЛИ ПРАВООБЛАДАТЕЛИ НЕ НЕСУТ ОТВЕТСТВЕННОСТИ ПО ИСКАМ О ВОЗМЕЩЕНИИ УЩЕРБА, УБЫТКОВ
 * ИЛИ ДРУГИХ ТРЕБОВАНИЙ ПО ДЕЙСТВУЮЩИМ КОНТРАКТАМ, ДЕЛИКТАМ ИЛИ ИНОМУ, ВОЗНИКШИМ ИЗ, ИМЕЮЩИМ
 * ПРИЧИНОЙ ИЛИ СВЯЗАННЫМ С ПРОГРАММНЫМ ОБЕСПЕЧЕНИЕМ ИЛИ ИСПОЛЬЗОВАНИЕМ ПРОГРАММНОГО ОБЕСПЕЧЕНИЯ
 * ИЛИ ИНЫМИ ДЕЙСТВИЯМИ С ПРОГРАММНЫМ ОБЕСПЕЧЕНИЕМ.
 */

package jcog.reflect;


import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Kamnev Georgiy (nt.gocha@gmail.com)
 */
public class ClobToString
        implements ToStringConverter {

    private static void logException(Throwable ex) {
        Logger.getLogger(ClobToString.class.getName()).log(Level.SEVERE, null, ex);
    }
    //</editor-fold>

    public static String stringOf(Clob clob) {
        if (clob == null) throw new IllegalArgumentException("clob==null");
        StringWriter sw = new StringWriter();
        writeClobTo(clob, sw);
        return sw.toString();
    }

    public static void writeClobTo(Clob clob, Writer writer) {
        if (clob == null) throw new IllegalArgumentException("clob==null");
        if (writer == null) throw new IllegalArgumentException("writer==null");

        try {
            Reader reader = clob.getCharacterStream();
            int buffSize = 1024 * 4;
            char[] buff = new char[buffSize];
            while (true) {
                int rd = reader.read(buff);
                if (rd < 0) break;
                if (rd > 0) {
                    writer.write(buff, 0, rd);
                }
            }
            reader.close();
        } catch (SQLException | IOException ex) {
            logException(ex);
        }
    }

    @Override
    public String apply(Object srcData) {
        if (srcData instanceof Clob) {
            return stringOf((Clob) srcData);
        }
        return null;
    }
}
