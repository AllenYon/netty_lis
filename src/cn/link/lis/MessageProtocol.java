package cn.link.lis;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.Delimiters;

/**
 * 0 1 2 3| xxxxxxxxxxxxx
 * Length |  Content
 */
public class MessageProtocol {

    private static final char K_MAGIC = 'x';
    public static final char K_START_GAME = 'a';
    public static final char K_SEND_LIP = 'b';
    public static final char K_SEND_CURRENT_INDEX = 'c';

    public static final char K_SERVER_WIN = 'd';
    public static final char K_CLIENT_WIN = 'e';

    public static final char K_CLIENT_CONFIRM_WIN = 'f';
    public static final char K_CLIENT_CONFIRM_LOST = 'g';
    public static final char K_SERVER_CONFIRM_WIN = 'h';
    public static final char K_SERVER_CONFIRM_LOST = 'i';

    public char magic;
    public char action;

    public LISProblem lisProblem;
    public int[] currentIndex;
    public boolean isWin;

    public long winTime;

    public MessageProtocol() {
        this.magic = 'x';
    }

    public static ByteBuf build(char action, Object... objects) {
        ByteBuf bb = Unpooled.buffer();
        bb.markReaderIndex();
        bb.writeInt(0).writeChar(K_MAGIC).writeChar(action);
        switch (action) {
            case K_SEND_CURRENT_INDEX:
                break;
            case K_CLIENT_WIN:
            case K_SERVER_WIN:
                Long winTime = (Long) objects[0];
                bb.writeLong(winTime.longValue());
                break;
        }
        bb.setInt(0, bb.readableBytes() - 4);
        return bb;
    }


    public static ByteBuf sendCurrentIndex(int[] indexs) {
        ByteBuf bb = Unpooled.buffer();
        bb.markReaderIndex();
        bb.writeInt(0).writeChar(K_MAGIC).writeChar(K_SEND_CURRENT_INDEX);
        bb.writeInt(indexs.length);
        for (int i = 0; i < indexs.length; i++) {
            bb.writeInt(indexs[i]);
        }
        bb.setInt(0, bb.readableBytes() - 4);
        return bb;
    }

    public static ByteBuf sendLIP(LISProblem lisProblem) {
        ByteBuf bb = Unpooled.buffer();
        bb.markReaderIndex();
        bb.writeInt(0).writeChar(K_MAGIC).writeChar(K_SEND_LIP);
        bb.writeInt(lisProblem.randomIntArray.length);
        for (int i = 0; i < lisProblem.randomIntArray.length; i++) {
            bb.writeInt(lisProblem.randomIntArray[i]);
        }
        bb.writeInt(lisProblem.bestSolve);
        for (int i = 0; i < lisProblem.bestSolveSuq.length; i++) {
            bb.writeInt(lisProblem.bestSolveSuq[i]);
        }
        bb.setInt(0, bb.readableBytes() - 4);
        return bb;
    }

    public static MessageProtocol parse(ByteBuf bb) {
        MessageProtocol msg = new MessageProtocol();
        msg.magic = bb.readChar();
        if (msg.magic != 'x') {
            return null;
        }
        msg.action = bb.readChar();
        switch (msg.action) {
            case K_START_GAME:
                break;
            case K_SEND_LIP:
                msg.lisProblem = new LISProblem();
                int leng_randomIntArray = bb.readInt();
                msg.lisProblem.randomIntArray = new int[leng_randomIntArray];
                for (int i = 0; i < leng_randomIntArray; i++) {
                    msg.lisProblem.randomIntArray[i] = bb.readInt();
                }
                msg.lisProblem.bestSolve = bb.readInt();
                msg.lisProblem.bestSolveSuq = new int[msg.lisProblem.bestSolve];
                for (int i = 0; i < msg.lisProblem.bestSolve; i++) {
                    msg.lisProblem.bestSolveSuq[i] = bb.readInt();
                }
                break;
            case K_SEND_CURRENT_INDEX: {
                int length = bb.readInt();
                msg.currentIndex = new int[length];
                for (int i = 0; i < length; i++) {
                    msg.currentIndex[i] = bb.readInt();
                }
                break;
            }
            case K_SERVER_WIN:
            case K_CLIENT_WIN:
                msg.winTime = bb.readLong();
                break;
        }
        return msg;
    }

    public char getAction() {
        return action;
    }

}
