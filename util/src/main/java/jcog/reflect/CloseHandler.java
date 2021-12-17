/*
 * The MIT License
 *
 * Copyright 2014 Kamnev Georgiy (nt.gocha@gmail.com).
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


import java.io.Closeable;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Отсоединение подписчика
 *
 * @author gocha
 */
public class CloseHandler implements Closeable, Runnable {
    /**
     * Пустой объект
     */
    public static final CloseHandler dummy = new CloseHandler() {
        @Override
        public void closeHandler() {
        }
    };
    protected Closeable closeable;
    protected Runnable runnable;

    /**
     * Конструктор по умолчанию
     */
    public CloseHandler() {
    }

    /**
     * Конструктор
     *
     * @param c кому делегировать вызов
     */
    @SuppressWarnings("LambdaUnfriendlyMethodOverload")
    public CloseHandler(Closeable c) {
        this.closeable = c;
    }

    /**
     * Конструктор
     *
     * @param r кому делегировать вызов
     */
    @SuppressWarnings("LambdaUnfriendlyMethodOverload")
    public CloseHandler(Runnable r) {
        this.runnable = r;
    }
    //</editor-fold>

    /**
     * Конструктор
     *
     * @param c кому делегировать вызов
     * @param r кому делегировать вызов
     */
    public CloseHandler(Closeable c, Runnable r) {
        this.closeable = c;
        this.runnable = r;
    }

    /**
     * Отсоединение подписчика
     */
    @Override
    public void close() {
        closeHandler();
    }

    /**
     * Отсоединение подписчика
     */
    @Override
    public void run() {
        closeHandler();
    }

    /**
     * Отсоединение подписчика
     */
    public void closeHandler() {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ex) {
                Logger.getLogger(CloseHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
            closeable = null;
        }
        if (runnable != null) {
            runnable.run();
            runnable = null;
        }
    }
}